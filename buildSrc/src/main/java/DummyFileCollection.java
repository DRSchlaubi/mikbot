import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.util.internal.GUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record DummyFileCollection(List<String> literalFiles) implements FileCollection {
    @Override
    public File getSingleFile() throws IllegalStateException {
        if (literalFiles.size() != 1) {
            throw new IllegalStateException("Found more than 1 file");
        } else {
            return new DummyFile(literalFiles.get(0));
        }
    }

    @Override
    public Set<File> getFiles() {
        return literalFiles.stream().map(DummyFile::new).collect(Collectors.toSet());
    }

    @Override
    public boolean contains(File file) {
        return getFiles().contains(file);
    }

    @Override
    public String getAsPath() {
        return GUtil.asPath(this);
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        return null;
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        return null;
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        return null;
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return literalFiles.isEmpty();
    }

    @Override
    public FileTree getAsFileTree() {
        return null;
    }

    @Override
    public Provider<Set<FileSystemLocation>> getElements() {
        return null;
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, AntType type) {

    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        return null;
    }

    @Override
    public Iterator<File> iterator() {
        return getFiles().iterator();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return new TaskDependency() {
            @Override
            public Set<? extends Task> getDependencies(@Nullable Task task) {
                return Set.of();
            }
        };
    }

    private class DummyFile extends File {
        public DummyFile(String pathname) {
            super(pathname);
        }

        @Override
        public String getName() {
            var string = toString();
            if (string.startsWith("lib")) {
                return string.substring(4);
            } else {
                return string;
            }
        }
    }
}
