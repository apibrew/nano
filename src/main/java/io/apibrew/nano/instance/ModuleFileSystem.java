package io.apibrew.nano.instance;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.io.FileSystem;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log4j2
@RequiredArgsConstructor
public class ModuleFileSystem implements FileSystem {

    private final GraalVmNanoEngine graalVmNanoEngine;

    @Override
    public Path parsePath(URI uri) {
        log.debug("parsePath: " + uri);
        return Paths.get(uri);
    }

    @Override
    public Path parsePath(String path) {
        log.debug("parsePath: " + path);
        return Paths.get(path);
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        log.debug("checkAccess: " + path);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        log.debug("createDirectory: " + dir);
    }

    @Override
    public void delete(Path path) throws IOException {
        log.debug("delete: " + path);
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        log.debug("newByteChannel: " + path);
        return Files.newByteChannel(path, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        log.debug("newDirectoryStream: " + dir);
        return Files.newDirectoryStream(dir);
    }

    @Override
    public Path toAbsolutePath(Path path) {
        log.debug("toAbsolutePath: " + path);
        return path;
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        log.debug("toRealPath: " + path);
        return path;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        log.debug("readAttributes: " + path);
        return new HashMap<>();
    }
}
