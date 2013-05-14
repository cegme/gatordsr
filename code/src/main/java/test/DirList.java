package test;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public class DirList {

	public static List getFileList(String dir) {
		final LinkedList<String> list = new LinkedList<String>();
		try {
			Path startPath = Paths.get(dir);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) {
					//System.out.println("Dir: " + dir.toString());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					list.add(file.toString());
					//System.out.println("File: " + file.toString());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DirList dir = new DirList();
		List l = DirList.getFileList("/home/morteza/Downloads");
		for (Object o: l)
			System.out.println(o);

	}

}
