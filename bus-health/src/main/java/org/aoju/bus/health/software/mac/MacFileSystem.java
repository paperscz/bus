/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.software.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFMutableDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.DiskArbitration;
import com.sun.jna.platform.mac.DiskArbitration.DADiskRef;
import com.sun.jna.platform.mac.DiskArbitration.DASessionRef;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.Statfs;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.common.mac.SysctlUtils;
import org.aoju.bus.health.software.FileSystem;
import org.aoju.bus.health.software.OSFileStore;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The Mac File System contains {@link OSFileStore}s which are
 * a storage pool, device, partition, volume, concrete file system or other
 * implementation specific means of file storage. In Mac OS X, these are found
 * in the /Volumes directory.
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public class MacFileSystem implements FileSystem {

    // Regexp matcher for /dev/disk1 etc.
    private static final Pattern LOCAL_DISK = Pattern.compile("/dev/disk\\d");

    /**
     * <p>
     * updateFileStoreStats.
     * </p>
     *
     * @param osFileStore a {@link OSFileStore} object.
     * @return a boolean.
     */
    public static boolean updateFileStoreStats(OSFileStore osFileStore) {
        for (OSFileStore fileStore : new MacFileSystem().getFileStoreMatching(osFileStore.getName())) {
            if (osFileStore.getVolume().equals(fileStore.getVolume())
                    && osFileStore.getMount().equals(fileStore.getMount())) {
                osFileStore.setLogicalVolume(fileStore.getLogicalVolume());
                osFileStore.setDescription(fileStore.getDescription());
                osFileStore.setType(fileStore.getType());
                osFileStore.setFreeSpace(fileStore.getFreeSpace());
                osFileStore.setUsableSpace(fileStore.getUsableSpace());
                osFileStore.setTotalSpace(fileStore.getTotalSpace());
                osFileStore.setFreeInodes(fileStore.getFreeInodes());
                osFileStore.setTotalInodes(fileStore.getTotalInodes());
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Gets File System Information.
     */
    @Override
    public OSFileStore[] getFileStores() {
        // List of file systems
        List<OSFileStore> fsList = getFileStoreMatching(null);
        return fsList.toArray(new OSFileStore[0]);
    }

    private List<OSFileStore> getFileStoreMatching(String nameToMatch) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Use getfsstat to find fileSystems
        // Query with null to get total # required
        int numfs = SystemB.INSTANCE.getfsstat64(null, 0, 0);
        if (numfs > 0) {
            // Open a DiskArbitration session to get VolumeName of file systems
            // with bsd names
            DASessionRef session = DiskArbitration.INSTANCE
                    .DASessionCreate(CoreFoundation.INSTANCE.CFAllocatorGetDefault());
            if (session == null) {
                Logger.error("Unable to open session to DiskArbitration framework.");
            } else {
                CFStringRef daVolumeNameKey = CFStringRef.createCFString("DAVolumeName");

                // Create array to hold results
                Statfs[] fs = new Statfs[numfs];
                // Fill array with results
                numfs = SystemB.INSTANCE.getfsstat64(fs, numfs * new Statfs().size(), SystemB.MNT_NOWAIT);
                for (int f = 0; f < numfs; f++) {
                    // Mount on name will match mounted path, e.g. /Volumes/foo
                    // Mount to name will match canonical path., e.g., /dev/disk0s2
                    // Byte arrays are null-terminated strings

                    // Get volume name
                    String volume = new String(fs[f].f_mntfromname, StandardCharsets.UTF_8).trim();
                    // Skip system types
                    if (volume.equals("devfs") || volume.startsWith("map ")) {
                        continue;
                    }
                    // Set description
                    String description = "Volume";
                    if (LOCAL_DISK.matcher(volume).matches()) {
                        description = "Local Disk";
                    }
                    if (volume.startsWith("localhost:") || volume.startsWith(Symbol.FORWARDSLASH)) {
                        description = "Network Drive";
                    }
                    // Set type and path
                    String type = new String(fs[f].f_fstypename, StandardCharsets.UTF_8).trim();
                    String path = new String(fs[f].f_mntonname, StandardCharsets.UTF_8).trim();

                    String name = "";
                    File file = new File(path);
                    if (name.isEmpty()) {
                        name = file.getName();
                        // getName() for / is still blank, so:
                        if (name.isEmpty()) {
                            name = file.getPath();
                        }
                    }
                    if (nameToMatch != null && !nameToMatch.equals(name)) {
                        continue;
                    }

                    String uuid = "";
                    // Use volume to find DiskArbitration volume name and search for
                    // the registry entry for UUID
                    String bsdName = volume.replace("/dev/disk", "disk");
                    if (bsdName.startsWith("disk")) {
                        // Get the DiskArbitration dictionary for this disk,
                        // which has volumename
                        DADiskRef disk = DiskArbitration.INSTANCE.DADiskCreateFromBSDName(
                                CoreFoundation.INSTANCE.CFAllocatorGetDefault(), session, volume);
                        if (disk != null) {
                            CFDictionaryRef diskInfo = DiskArbitration.INSTANCE.DADiskCopyDescription(disk);
                            if (diskInfo != null) {
                                // get volume name from its key
                                Pointer result = diskInfo.getValue(daVolumeNameKey);
                                CFStringRef volumePtr = new CFStringRef(result);
                                name = volumePtr.stringValue();
                                if (name == null) {
                                    name = Builder.UNKNOWN;
                                }
                                diskInfo.release();
                            }
                            disk.release();
                        }
                        // Search for bsd name in IOKit registry for UUID
                        CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
                        if (matchingDict != null) {
                            // search for all IOservices that match the bsd name
                            IOIterator fsIter = IOKitUtil.getMatchingServices(matchingDict);
                            if (fsIter != null) {
                                // getMatchingServices releases matchingDict
                                // Should only match one logical drive
                                IORegistryEntry fsEntry = fsIter.next();
                                if (fsEntry != null && fsEntry.conformsTo("IOMedia")) {
                                    // Now get the UUID
                                    uuid = fsEntry.getStringProperty("UUID");
                                    if (uuid != null) {
                                        uuid = uuid.toLowerCase();
                                    }
                                    fsEntry.release();
                                }
                                fsIter.release();
                            }
                        }
                    }

                    // Add to the list
                    OSFileStore osStore = new OSFileStore();
                    osStore.setName(name);
                    osStore.setVolume(volume);
                    osStore.setMount(path);
                    osStore.setDescription(description);
                    osStore.setType(type);
                    osStore.setUUID(uuid == null ? "" : uuid);
                    osStore.setFreeSpace(file.getFreeSpace());
                    osStore.setUsableSpace(file.getUsableSpace());
                    osStore.setTotalSpace(file.getTotalSpace());
                    osStore.setFreeInodes(fs[f].f_ffree);
                    osStore.setTotalInodes(fs[f].f_files);
                    fsList.add(osStore);
                }
                daVolumeNameKey.release();
                // Close DA session
                session.release();
            }
        }
        return fsList;
    }

    @Override
    public long getOpenFileDescriptors() {
        return SysctlUtils.sysctl("kern.num_files", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return SysctlUtils.sysctl("kern.maxfiles", 0);
    }
}
