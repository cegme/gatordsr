package edu.ufl.cise;

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

	/**
	 * List of files in a directory froma ssh session.
	 * 
	 * @param dir
	 * @return
	 */
	public static List<String> getSSHFileList(String dir) {

		String command = "sshpass -p 'trecGuest' ssh trecGuest@sm321-01.cise.ufl.edu "
				+ "'ls /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
				+ dir + "'";

		List<String> list = FileProcessor.runStringShellCommand(command);

		return list;
	}

	/**
	 * if filter is not null the file names containing that specific filter string will be returned.
	 * 
	 * @param dir
	 * @param filter
	 * @return
	 */
	public static List<String> getFileList(String dir, final String filter) {
		final LinkedList<String> list = new LinkedList<String>();
		try {
			Path startPath = Paths.get(dir);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// System.out.println("Dir: " + dir.toString());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					String fileName = file.toString();
					if (filter != null && fileName.contains(filter))
						list.add(fileName);
					else if (filter == null)
						list.add(fileName);
					// System.out.println("File: " + file.toString());
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

	// public static List getFileList(String dir) {
	// return getFileList(dir, "");
	// }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// List l = DirList.getFileList("/home/morteza/Downloads", "");
		List l = DirList.getSSHFileList("2011-11-04-07");
		for (Object o : l)
			System.out.println(o);

	}

}
