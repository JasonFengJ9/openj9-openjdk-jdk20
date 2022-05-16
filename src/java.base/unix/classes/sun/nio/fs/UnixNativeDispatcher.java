/*
 * Copyright (c) 2008, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.nio.fs;

import java.util.function.Function;
import jdk.internal.misc.Blocker;

/**
 * Unix system and library calls.
 */

class UnixNativeDispatcher {
    protected UnixNativeDispatcher() { }

    // returns a NativeBuffer containing the given path
    static NativeBuffer copyToNativeBuffer(UnixPath path) {
        byte[] cstr = path.getByteArrayForSysCalls();
        int size = cstr.length + 1;
        NativeBuffer buffer = NativeBuffers.getNativeBufferFromCache(size);
        if (buffer == null) {
            buffer = NativeBuffers.allocNativeBuffer(size);
        } else {
            // buffer already contains the path
            if (buffer.owner() == path)
                return buffer;
        }
        NativeBuffers.copyCStringToNativeBuffer(cstr, buffer);
        buffer.setOwner(path);
        return buffer;
    }

    /**
     * char *getcwd(char *buf, size_t size);
     */
    static native byte[] getcwd();

    /**
     * int dup(int filedes)
     */
    static native int dup(int filedes) throws UnixException;

    /**
     * int open(const char* path, int oflag, mode_t mode)
     */
    static int open(UnixPath path, int flags, int mode) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return open0(buffer.address(), flags, mode);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native int open0(long pathAddress, int flags, int mode)
        throws UnixException;

    /**
     * int openat(int dfd, const char* path, int oflag, mode_t mode)
     */
    static int openat(int dfd, byte[] path, int flags, int mode) throws UnixException {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return openat0(dfd, buffer.address(), flags, mode);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native int openat0(int dfd, long pathAddress, int flags, int mode)
        throws UnixException;

    /**
     * close(int filedes). If fd is -1 this is a no-op.
     */
    static void close(int fd) throws UnixException {
        if (fd != -1) {
            close0(fd);
        }
    }
    private static native void close0(int fd) throws UnixException;

    /**
     * close(fd). If close fails then the given exception supplier function is
     * invoked to produce an exception to throw. If the function returns null
     * then no exception is thrown. If close fails and the exception supplier
     * function is null, then no exception is thrown.
     */
    static <X extends Throwable>
    void close(int fd, Function<UnixException, X> mapper) throws X {
        try {
            close(fd);
        } catch (UnixException e) {
            if (mapper != null) {
                X ex = mapper.apply(e);
                if (ex != null) throw ex;
            }
        }
    }

    /**
     * void rewind(FILE* stream);
     */
    static native void rewind(long stream) throws UnixException;

    /**
     * ssize_t getline(char **lineptr, size_t *n, FILE *stream);
     */
    static native int getlinelen(long stream) throws UnixException;

    /**
     * link(const char* existing, const char* new)
     */
    static void link(UnixPath existing, UnixPath newfile) throws UnixException {
        try (NativeBuffer existingBuffer = copyToNativeBuffer(existing);
             NativeBuffer newBuffer = copyToNativeBuffer(newfile)) {
            long comp = Blocker.begin();
            try {
                link0(existingBuffer.address(), newBuffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void link0(long existingAddress, long newAddress)
        throws UnixException;

    /**
     * unlink(const char* path)
     */
    static void unlink(UnixPath path) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                unlink0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void unlink0(long pathAddress) throws UnixException;

    /**
     * unlinkat(int dfd, const char* path, int flag)
     */
    static void unlinkat(int dfd, byte[] path, int flag) throws UnixException {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                unlinkat0(dfd, buffer.address(), flag);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void unlinkat0(int dfd, long pathAddress, int flag)
        throws UnixException;

    /**
     * mknod(const char* path, mode_t mode, dev_t dev)
     */
    static void mknod(UnixPath path, int mode, long dev) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                mknod0(buffer.address(), mode, dev);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void mknod0(long pathAddress, int mode, long dev)
        throws UnixException;

    /**
     *  rename(const char* old, const char* new)
     */
    static void rename(UnixPath from, UnixPath to) throws UnixException {
        try (NativeBuffer fromBuffer = copyToNativeBuffer(from);
             NativeBuffer toBuffer = copyToNativeBuffer(to)) {
            long comp = Blocker.begin();
            try {
                rename0(fromBuffer.address(), toBuffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void rename0(long fromAddress, long toAddress)
        throws UnixException;

    /**
     *  renameat(int fromfd, const char* old, int tofd, const char* new)
     */
    static void renameat(int fromfd, byte[] from, int tofd, byte[] to) throws UnixException {
        try (NativeBuffer fromBuffer = NativeBuffers.asNativeBuffer(from);
             NativeBuffer toBuffer = NativeBuffers.asNativeBuffer(to)) {
            long comp = Blocker.begin();
            try {
                renameat0(fromfd, fromBuffer.address(), tofd, toBuffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void renameat0(int fromfd, long fromAddress, int tofd, long toAddress)
        throws UnixException;

    /**
     * mkdir(const char* path, mode_t mode)
     */
    static void mkdir(UnixPath path, int mode) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                mkdir0(buffer.address(), mode);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void mkdir0(long pathAddress, int mode) throws UnixException;

    /**
     * rmdir(const char* path)
     */
    static void rmdir(UnixPath path) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                rmdir0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void rmdir0(long pathAddress) throws UnixException;

    /**
     * readlink(const char* path, char* buf, size_t bufsize)
     *
     * @return  link target
     */
    static byte[] readlink(UnixPath path) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return readlink0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native byte[] readlink0(long pathAddress) throws UnixException;

    /**
     * realpath(const char* path, char* resolved_name)
     *
     * @return  resolved path
     */
    static byte[] realpath(UnixPath path) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return realpath0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native byte[] realpath0(long pathAddress) throws UnixException;

    /**
     * symlink(const char* name1, const char* name2)
     */
    static void symlink(byte[] name1, UnixPath name2) throws UnixException {
        try (NativeBuffer targetBuffer = NativeBuffers.asNativeBuffer(name1);
             NativeBuffer linkBuffer = copyToNativeBuffer(name2)) {
            long comp = Blocker.begin();
            try {
                symlink0(targetBuffer.address(), linkBuffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void symlink0(long name1, long name2)
        throws UnixException;

    /**
     * stat(const char* path, struct stat* buf)
     */
    static void stat(UnixPath path, UnixFileAttributes attrs) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                stat0(buffer.address(), attrs);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void stat0(long pathAddress, UnixFileAttributes attrs)
        throws UnixException;


    /**
     * stat(const char* path, struct stat* buf)
     *
     * @return st_mode (file type and mode) or 0 if an error occurs.
     */
    static int stat(UnixPath path) {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return stat1(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native int stat1(long pathAddress);


    /**
     * lstat(const char* path, struct stat* buf)
     */
    static void lstat(UnixPath path, UnixFileAttributes attrs) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                lstat0(buffer.address(), attrs);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void lstat0(long pathAddress, UnixFileAttributes attrs)
        throws UnixException;

    /**
     * fstat(int filedes, struct stat* buf)
     */
    static void fstat(int fd, UnixFileAttributes attrs) throws UnixException {
        long comp = Blocker.begin();
        try {
            fstat0(fd, attrs);
        } finally {
            Blocker.end(comp);
        }
    }
    private static native void fstat0(int fd, UnixFileAttributes attrs)
        throws UnixException;

    /**
     * fstatat(int filedes,const char* path,  struct stat* buf, int flag)
     */
    static void fstatat(int dfd, byte[] path, int flag, UnixFileAttributes attrs)
        throws UnixException
    {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                fstatat0(dfd, buffer.address(), flag, attrs);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void fstatat0(int dfd, long pathAddress, int flag,
        UnixFileAttributes attrs) throws UnixException;

    /**
     * chown(const char* path, uid_t owner, gid_t group)
     */
    static void chown(UnixPath path, int uid, int gid) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                chown0(buffer.address(), uid, gid);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void chown0(long pathAddress, int uid, int gid)
        throws UnixException;

    /**
     * lchown(const char* path, uid_t owner, gid_t group)
     */
    static void lchown(UnixPath path, int uid, int gid) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                lchown0(buffer.address(), uid, gid);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void lchown0(long pathAddress, int uid, int gid)
        throws UnixException;

    /**
     * fchown(int filedes, uid_t owner, gid_t group)
     */
    static void fchown(int fd, int uid, int gid) throws UnixException {
        long comp = Blocker.begin();
        try {
            fchown0(fd, uid, gid);
        } finally {
            Blocker.end(comp);
        }
    }
    static native void fchown0(int fd, int uid, int gid) throws UnixException;

    /**
     * chmod(const char* path, mode_t mode)
     */
    static void chmod(UnixPath path, int mode) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                chmod0(buffer.address(), mode);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void chmod0(long pathAddress, int mode)
        throws UnixException;

    /**
     * fchmod(int fildes, mode_t mode)
     */
    static void fchmod(int fd, int mode) throws UnixException {
        long comp = Blocker.begin();
        try {
            fchmod0(fd, mode);
        } finally {
            Blocker.end(comp);
        }
    }
    private static native void fchmod0(int fd, int mode) throws UnixException;

    /**
     * utimes(const char* path, const struct timeval times[2])
     */
    static void utimes(UnixPath path, long times0, long times1)
        throws UnixException
    {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                utimes0(buffer.address(), times0, times1);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void utimes0(long pathAddress, long times0, long times1)
        throws UnixException;

    /**
     * futimes(int fildes, const struct timeval times[2])
     */
    static void futimes(int fd, long times0, long times1) throws UnixException {
        long comp = Blocker.begin();
        try {
            futimes0(fd, times0, times1);
        } finally {
            Blocker.end(comp);
        }
    }
    private static native void futimes0(int fd, long times0, long times1)
        throws UnixException;

    /**
     * futimens(int fildes, const struct timespec times[2])
     */
    static void futimens(int fd, long times0, long times1) throws UnixException {
        long comp = Blocker.begin();
        try {
            futimens0(fd, times0, times1);
        } finally {
            Blocker.end(comp);
        }
    }
    private static native void futimens0(int fd, long times0, long times1)
        throws UnixException;

    /**
     * lutimes(const char* path, const struct timeval times[2])
     */
    static void lutimes(UnixPath path, long times0, long times1)
        throws UnixException
    {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                lutimes0(buffer.address(), times0, times1);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void lutimes0(long pathAddress, long times0, long times1)
        throws UnixException;

    /**
     * DIR *opendir(const char* dirname)
     */
    static long opendir(UnixPath path) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return opendir0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native long opendir0(long pathAddress) throws UnixException;

    /**
     * DIR* fdopendir(int filedes)
     */
    static native long fdopendir(int dfd) throws UnixException;


    /**
     * closedir(DIR* dirp)
     */
    static native void closedir(long dir) throws UnixException;

    /**
     * struct dirent* readdir(DIR *dirp)
     *
     * @return  dirent->d_name
     */
    static byte[] readdir(long dir) throws UnixException {
        long comp = Blocker.begin();
        try {
            return readdir0(dir);
        } finally {
            Blocker.end(comp);
        }
    }
    static native byte[] readdir0(long dir) throws UnixException;

    /**
     * size_t read(int fildes, void* buf, size_t nbyte)
     */
    static int read(int fildes, long buf, int nbyte) throws UnixException {
        long comp = Blocker.begin();
        try {
            return read0(fildes, buf, nbyte);
        } finally {
            Blocker.end(comp);
        }
    }
    private static native int read0(int fildes, long buf, int nbyte) throws UnixException;

    /**
     * size_t writeint fildes, void* buf, size_t nbyte)
     */
    static int write(int fildes, long buf, int nbyte) throws UnixException {
        long comp = Blocker.begin();
        try {
            return write0(fildes, buf, nbyte);
        } finally {
            Blocker.end(comp);
        }
    }
    private static native int write0(int fildes, long buf, int nbyte) throws UnixException;

    /**
     * access(const char* path, int amode);
     */
    static void access(UnixPath path, int amode) throws UnixException {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                access0(buffer.address(), amode);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void access0(long pathAddress, int amode) throws UnixException;

    /**
     * access(constant char* path, F_OK)
     *
     * @return true if the file exists, false otherwise
     */
    static boolean exists(UnixPath path) {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                return exists0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native boolean exists0(long pathAddress);

    /**
     * struct passwd *getpwuid(uid_t uid);
     *
     * @return  passwd->pw_name
     */
    static native byte[] getpwuid(int uid) throws UnixException;

    /**
     * struct group *getgrgid(gid_t gid);
     *
     * @return  group->gr_name
     */
    static native byte[] getgrgid(int gid) throws UnixException;

    /**
     * struct passwd *getpwnam(const char *name);
     *
     * @return  passwd->pw_uid
     */
    static int getpwnam(String name) throws UnixException {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(Util.toBytes(name))) {
            long comp = Blocker.begin();
            try {
                return getpwnam0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native int getpwnam0(long nameAddress) throws UnixException;

    /**
     * struct group *getgrnam(const char *name);
     *
     * @return  group->gr_name
     */
    static int getgrnam(String name) throws UnixException {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(Util.toBytes(name))) {
            long comp = Blocker.begin();
            try {
                return getgrnam0(buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native int getgrnam0(long nameAddress) throws UnixException;

    /**
     * statvfs(const char* path, struct statvfs *buf)
     */
    static void statvfs(UnixPath path, UnixFileStoreAttributes attrs)
        throws UnixException
    {
        try (NativeBuffer buffer = copyToNativeBuffer(path)) {
            long comp = Blocker.begin();
            try {
                statvfs0(buffer.address(), attrs);
            } finally {
                Blocker.end(comp);
            }
        }
    }
    private static native void statvfs0(long pathAddress, UnixFileStoreAttributes attrs)
        throws UnixException;

    /**
     * char* strerror(int errnum)
     */
    static native byte[] strerror(int errnum);

    /**
     * ssize_t fgetxattr(int filedes, const char *name, void *value, size_t size);
     */
    static int fgetxattr(int filedes, byte[] name, long valueAddress, int valueLen)
        throws UnixException
    {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(name)) {
            long comp = Blocker.begin();
            try {
                return fgetxattr0(filedes, buffer.address(), valueAddress, valueLen);
            } finally {
                Blocker.end(comp);
            }
        }
    }

    private static native int fgetxattr0(int filedes, long nameAddress,
        long valueAddress, int valueLen) throws UnixException;

    /**
     *  fsetxattr(int filedes, const char *name, const void *value, size_t size, int flags);
     */
    static void fsetxattr(int filedes, byte[] name, long valueAddress, int valueLen)
        throws UnixException
    {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(name)) {
            long comp = Blocker.begin();
            try {
                fsetxattr0(filedes, buffer.address(), valueAddress, valueLen);
            } finally {
                Blocker.end(comp);
            }
        }
    }

    private static native void fsetxattr0(int filedes, long nameAddress,
        long valueAddress, int valueLen) throws UnixException;

    /**
     * fremovexattr(int filedes, const char *name);
     */
    static void fremovexattr(int filedes, byte[] name) throws UnixException {
        try (NativeBuffer buffer = NativeBuffers.asNativeBuffer(name)) {
            long comp = Blocker.begin();
            try {
                fremovexattr0(filedes, buffer.address());
            } finally {
                Blocker.end(comp);
            }
        }
    }

    private static native void fremovexattr0(int filedes, long nameAddress)
        throws UnixException;

    /**
     * size_t flistxattr(int filedes, const char *list, size_t size)
     */
    static native int flistxattr(int filedes, long listAddress, int size)
        throws UnixException;

    /**
     * Capabilities
     */
    private static final int SUPPORTS_OPENAT        = 1 << 1;  // syscalls
    private static final int SUPPORTS_FUTIMES       = 1 << 2;
    private static final int SUPPORTS_FUTIMENS      = 1 << 3;
    private static final int SUPPORTS_LUTIMES       = 1 << 4;
    private static final int SUPPORTS_XATTR         = 1 << 5;
    private static final int SUPPORTS_BIRTHTIME     = 1 << 16; // other features
    private static final int capabilities;

    /**
     * Supports openat and other *at calls.
     */
    static boolean openatSupported() {
        return (capabilities & SUPPORTS_OPENAT) != 0;
    }

    /**
     * Supports futimes or futimesat
     */
    static boolean futimesSupported() {
        return (capabilities & SUPPORTS_FUTIMES) != 0;
    }

    /**
     * Supports futimens
     */
    static boolean futimensSupported() {
        return (capabilities & SUPPORTS_FUTIMENS) != 0;
    }

    /**
     * Supports lutimes
     */
    static boolean lutimesSupported() {
        return (capabilities & SUPPORTS_LUTIMES) != 0;
    }

    /**
     * Supports file birth (creation) time attribute
     */
    static boolean birthtimeSupported() {
        return (capabilities & SUPPORTS_BIRTHTIME) != 0;
    }

    /**
     * Supports extended attributes
     */
    static boolean xattrSupported() {
        return (capabilities & SUPPORTS_XATTR) != 0;
    }

    private static native int init();
    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
        capabilities = init();
    }
}
