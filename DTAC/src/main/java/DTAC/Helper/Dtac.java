package DTAC.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import com.healthmarketscience.jackcess.*;

public class Dtac {

	public static void main(String[] args) throws IOException, ZipException {

		String exportFolder = (getExportFolder());

		File cruFiles = new File(exportFolder);

		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cwu");
			}
		};

		String[] cruFilesNames = cruFiles.list(filter);

		checkCwuFiles(exportFolder, cruFilesNames);
		
	}

	private static void checkCwuFiles(String exportFolder, String[] cruFilesNames) throws ZipException, IOException {
		for (String fileName : cruFilesNames) {
			
			String databasePatch = unzipCruFile(new File(exportFolder + fileName));
			
			boolean check = checkDatabase(databasePatch);
			System.out.println(check);
			
			if (!check) {
				
				repairDatabase(exportFolder, databasePatch);
				
			} else {
				
				FileUtils.deleteDirectory(new File(databasePatch));
				System.out.println("jest ok");
			}
		}
	}

	private static void repairDatabase(String exportFolder, String databasePatch) throws ZipException, IOException {
		String newCwuFileName = zipFile(databasePatch);				

		changeExtension(new File(newCwuFileName), "cwu");

		FileUtils.copyFileToDirectory(new File(newCwuFileName.replace(".zip", ".cwu")), new File(exportFolder));

		System.out.println("naprawiono");
	}

	public static String zipFile(String databasePatch) throws ZipException {

		ArrayList<File> fileList = new ArrayList<File>();

		File[] filesToPack = (new File(databasePatch)).listFiles();
		for (File file : filesToPack) {
			fileList.add(file);
		}

		String fileName = databasePatch.substring(databasePatch.lastIndexOf("\\"));
		String newCwuFileName = databasePatch + "\\" + fileName + ".zip";
		ZipFile zipFile = new ZipFile(newCwuFileName);
		ZipParameters zip4jZipParameters = new ZipParameters();
		zip4jZipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		zip4jZipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		zipFile.addFiles(fileList, zip4jZipParameters);

		return newCwuFileName;
	}

	public static String unzipCruFile(File file) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(file.getCanonicalPath());
		String databasePatch = FilenameUtils.getFullPath(file.getAbsolutePath())
				+ FilenameUtils.getBaseName(file.getName());
		zipFile.extractAll(databasePatch);
		return databasePatch;
	}

	public static File changeExtension(File file, String extension) {
		String filename = file.getName();

		if (filename.contains(".")) {
			filename = filename.substring(0, filename.lastIndexOf('.'));
		}
		filename += "." + extension;

		file.renameTo(new File(file.getParentFile(), filename));
		return file;
	}

	private static String getExportFolder() throws IOException {

		String tmp = FileUtils.readFileToString(new File("conf.txt"), StandardCharsets.UTF_8);
		tmp = tmp.substring(tmp.indexOf("=") + 1).toString().replace("\\\\", "\\");

		return tmp.replace("\r\n", "");
	}

	public static boolean checkDatabase(String databasePatch) throws IOException {

		// true - baza nie zawiera bledow
		// false - baza z bledami, naprawiono

		boolean result = true;

		Database db = DatabaseBuilder.open(new File(databasePatch + "\\unit.mdb"));
		Table table = db.getTable("Projects");

		for (Row row : table) {
			if (row.containsValue("Mariusz Ziolkowski test prep")) {
				table.deleteRow(row);
				result = false;
			}
		}

		db.close();

		return result;
	}
}
