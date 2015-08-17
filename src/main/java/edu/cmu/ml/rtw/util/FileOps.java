package edu.cmu.ml.rtw.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Utility class related to working with files.
 * Most of the code is from NELL@CMU codebase
 */
public class FileOps {

  //  private static final Logger log = Logger.getLogger(FileUtility.class);

  /** Disallow construction */
  private FileOps() {
  };

  public static FilenameFilter noHiddenFilesFilter = new FilenameFilter() {

    public boolean accept(File dir, String name) {
      return !name.startsWith(".");
    }
  };

  public static class regexFileFilter implements FilenameFilter {

    protected Pattern filePattern;

    public boolean accept(File dir, String name) {
      return filePattern.matcher(name).find();
    }

    public regexFileFilter(String regex) {
      filePattern = Pattern.compile(regex);
    }
  };

  public static FilenameFilter directoryFileFilter = new FilenameFilter() {

    public boolean accept(File dir, String name) {
      File child = new File(dir, name);
      return child.isDirectory();
    }
  };

  /** Returns a list of all files in a directory */
  public static String[] buildFileList(File dir) throws Exception {
    List<String> fileVectorList = listFiles(dir, new ArrayList<String>());
    return (String[]) fileVectorList.toArray(new String[0]);
  }

  /** Used by buildFileList to list all files in a directory */
  private  static List<String> listFiles(File dir, List<String> fileVectorList) {
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
        listFiles((File) files[i], fileVectorList);
    } else fileVectorList.add(dir.getAbsolutePath());

    return fileVectorList;
  }

  //      /**
  //       * Reads the contents of the given file into a String to Integer Map.
  //       * 
  //       * @param filename
  //       *            the file to read from
  //       * @return the contents of the file
  //       */
  //      public static Map<String, Integer> readFileIntoMap(String filename) {
  //          Map<String, Integer> res = new HashMap<String, Integer>();
  //          try {
  //              BufferedFileReader in = new BufferedFileReader(filename);
  //              String line = in.readLine();
  //              while (line != null) {
  //                  String[] parts = line.split("\t");
  //                  res.put(parts[0], Integer.parseInt(parts[1].trim()));
  //                  line = in.readLine();
  //              }
  //              in.close();
  //          } catch (Exception e) {
  //              throw new RuntimeException("readFileIntoMap(\"" + filename + "\")", e);
  //          }
  //          return res;
  //      }

  /**
   * Reads the contents of the given file into one string.
   * 
   * @param filename
   *            the file to read from
   * @return the contents of the file
   */
  public static String readFileIntoString(String filename) {
    try {
      return Files.toString(new File(filename), Charsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("readFileIntoString(\"" + filename + "\")", e);
    }
  }

  /**
   * Method that should be 4-5x faster than using grepFirst for doing hit count cache lookups.
   * 
   * @param query
   *            the query to look up the hit count for
   * @param filename
   *            the file to read from
   * @return the substring from the rest of the line that matches the query
   * 
   */
  public static String queryCacheLookup(String query, String filename) {
    String prefix = query + ":: ";
    try {
      BufferedReader in = new BufferedReader(new FileReader(filename));
      String line = in.readLine();
      while (line != null) {
        if (line.startsWith(prefix)) {
          String result = line.substring(prefix.length());
          return result;
        }
        line = in.readLine();
      }
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("queryCacheLookup(\"" + query + "\", \"" + filename + "\")", e);
    }
    return null;
  }

  /**
   * Appends line to the file with path filename.
   */
  public static void appendLine(String line, String filename) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
      out.write(line);
      out.newLine();
      out.close();
    } catch (Exception e) {
      throw new RuntimeException("appendLine(\"" + line + "\", \"" + filename + "\")", e);
    }
  }

  /**
   * Appends the contents of the first file to the second file
   */
  public static void appendFile(File f1, File f2) {
    try {
      BufferedReader in = new BufferedReader(new FileReader(f1));
      BufferedWriter out = new BufferedWriter(new FileWriter(f2, true));
      String line = "";
      while ((line = in.readLine()) != null) {
        out.write(line);
        out.newLine();
      }
      in.close();
      out.close();
    } catch (Exception e) {
      throw new RuntimeException("appendFile(" + f1.getName() + ", " + f2.getName() + ")", e);
    }
  }

  /**
   * Writes the given string to the given file.
   * 
   * @param text
   *            the string to write
   * @param filename
   *            the file to write to
   */
  public static void writeStringToFile(String text, String filename) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename));
      out.write(text);
      out.newLine();
      out.close();
    } catch (Exception e) {
      String curDir = System.getProperty("user.dir");
      System.err.println("Current working directory: " + curDir);
      String dir = filename.substring(0, filename.lastIndexOf("/"));
      File f = new File(dir);
      if (!f.exists()) {
        System.err.println("directory doesn't exist: " + dir);
      }

      throw new RuntimeException("writeStringToFile(<text>, \"" + filename + "\")", e);
    }
  }

  /**
   * Writes the given string to the given file.
   * 
   * @param text
   *            the string to write
   * @param filename
   *            the file to write to
   */
  public static void writeListToFile(List<String> list, String filename) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename));
      for (String s : list) {
        out.write(s);
        out.newLine();
      }
      out.close();
    } catch (Exception e) {
      String curDir = System.getProperty("user.dir");
      System.out.println("Current working directory: " + curDir);
      String dir = filename.substring(0, filename.lastIndexOf("/"));
      File f = new File(dir);
      if (!f.exists()) {
        System.out.println("directory doesn't exist: " + dir);
      }

      throw new RuntimeException("writeStringToFile(<list>, \"" + filename + "\")", e);
    }
  }

  /**
   * Writes the given string to the given file.
   * 
   * @param text
   *            the string to write
   * @param file
   *            the file to write to
   */
  public static void writeListToFile(List<String> list, File file) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      for (String s : list) {
        out.write(s);
        out.newLine();
      }
      out.close();
    } catch (Exception e) {
      throw new RuntimeException("writeStringToFile(<list>, " + file + ")", e);
    }
  }

  public static void copyFile(String srcFileName, String destFileName) {
    try {
      File src = new File(srcFileName);
      File dst = new File(destFileName);
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);

      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    } catch (Exception e) {
      throw new RuntimeException("copyFile(\"" + srcFileName + "\", \"" + destFileName + "\")", e);
    }
  }

  public static void moveFile(String srcFileName, String destFileName) {
    try {
      File src = new File(srcFileName);
      File dst = new File(destFileName);
      File parent = new File(dst.getParent());
      if (!parent.exists()) parent.mkdirs();

      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);

      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
      src.delete();
    } catch (Exception e) {
      throw new RuntimeException("moveFile(\"" + srcFileName + "\", \"" + destFileName + "\")", e);
    }
  }

  /**
   * Reads the contents of the file with path filename into a list, one String per line. Trims
   * each line.
   * 
   * @param filename
   * @return
   */
  public static List<String> readFileIntoList(InputStream fileInput) {
    BufferedReader in;
    try {
      in = new BufferedReader(new InputStreamReader(fileInput));
      return readFileIntoList(in);
    } catch (Exception e) {
      throw new RuntimeException("readFileIntoList(\"" + fileInput + "\")", e);
    }
  }

  /**
   * Reads the contents of the file with path filename into a list, one String per line. Trims
   * each line.
   * 
   * @param file
   * @return
   */
  public static List<String> readFileIntoList(File file) {
    BufferedReader in;
    try {
      if ((in = new BufferedReader(new FileReader(file))) == null) {
        System.out.println("File not found: " + file);
        System.exit(0);
      }
      return readFileIntoList(in);
    } catch (Exception e) {
      throw new RuntimeException("readFileIntoList(" + file + ")", e);
    }
  }

  /**
   * Reads the contents of the file with path filename into a list, one String per line. Trims
   * each line.
   * 
   * @param filename
   * @return
   */
  public static List<String> readFileIntoList(String filename) {
    BufferedReader in;
    try {
      if ((in = new BufferedReader(new FileReader(filename))) == null) {
        System.out.println("File not found: " + filename);
        System.exit(0);
      }
      return readFileIntoList(in);
    } catch (Exception e) {
      throw new RuntimeException("readFileIntoList(\"" + filename + "\")", e);
    }
  }

  /**
   * Reads the contents of the file with path filename into a list, one String per line. Trims
   * each line.
   * 
   * @param filename
   * @return
   */
  public static List<String> readFileIntoList(BufferedReader in) {
    List<String> result = new ArrayList<String>();
    try {
      while (in.ready()) {
        String s = in.readLine().trim();
        if (s.length() > 0) result.add(s);
      }
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("readFileIntoList(\"" + in + "\")", e);
    }

    return result;
  }

  /**
   * Reads the contents of the file with path filename into a list, one String per line. Trims
   * each line. Ignores any lines that start with hash character.
   * 
   * @param filename
   * @return
   */
  public static List<String> readFileIntoListWithoutComments(String filename) {
    List<String> result = new ArrayList<String>();
    BufferedReader in;
    try {
      if ((in = new BufferedReader(new FileReader(filename))) == null) {
        System.out.println("File not found: " + filename);
        System.exit(0);
      }
      while (in.ready()) {
        String s = in.readLine().trim();
        if (s.length() > 0 && !s.startsWith("#")) result.add(s);
      }
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("readFileIntoList(\"" + filename + "\")", e);
    }

    return result;
  }

  static public void deleteFile(String path) {
    File npf = new File(path);
    if (npf.exists()) {
      npf.delete();
    }
  }

  // copied from http://www.rgagnon.com/javadetails/java-0483.html
  static public boolean deleteDirectory(File path) {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDirectory(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return (path.delete());
  }

  /**
   * Deletes all files in directory matching javaRegex
   */
  static public void deleteFiles(File directory, String javaRegex) {
    try {
      if (directory.exists()) {
        final Pattern filePattern = Pattern.compile(javaRegex);
        File[] files = directory.listFiles(new FilenameFilter() {

          public boolean accept(File dir, String name) {
            return filePattern.matcher(name).matches();
          }
        });
        for (File f : files) {
          // log.debug("Deleting " + f.toString() + " for matching " + javaRegex);
          f.delete();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("deleteFiles(" + directory + ", \"" + javaRegex + ")", e);
    }
  }

  /**
   * Deletes all files in directory matching javaRegex, and then recurses into and does the same
   * for all subdirectories of directory.
   */
  static public void deleteFilesRecursively(File directory, String javaRegex) {
    try {
      if (directory.exists()) {
        final Pattern filePattern = Pattern.compile(javaRegex);
        for (File f : directory.listFiles(new regexFileFilter(javaRegex))) {
          // log.debug("Deleting " + f.toString() + " for matching " + javaRegex);
          f.delete();
        }
        for (File f : directory.listFiles(directoryFileFilter)) {
          deleteFilesRecursively(f, javaRegex);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("deleteFiles(" + directory + ", \"" + javaRegex + ")", e);
    }
  }

  // copied from
  // http://www.java-tips.org/java-se-tips/java.io/how-to-copy-a-directory-from-one-location-to-another-loc.html
  // If targetLocation does not exist, it will be created.
  public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdirs();
      }

      String[] children = sourceLocation.list();
      for (int i = 0; i < children.length; i++) {
        copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
      }
    } else {

      InputStream in = new FileInputStream(sourceLocation);
      OutputStream out = new FileOutputStream(targetLocation);

      // Copy the bits from instream to outstream
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    }
  }

  //      /**
  //       * Return full path to a system binary
  //       *
  //       * This is meant for getting the location of common programs like "rm" or "gzip" that might be
  //       * in a slightly different place on each machine.
  //       *
  //       * An exception will be thrown if the desired binary cannot be located.
  //       */
  //      public static String findBinary(String name) {
  //          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  //          String whichName = "which";
  //          int status = QuickProcessExecutor.exec(Lists.newArrayList(whichName, name), null, buffer,
  //                  System.err);
  //          if (status != 0)
  //              throw new RuntimeException("Unable to locate \"" + name + "\" binary (status " + status
  //                      + ")");
  //          String found = buffer.toString().trim();
  //          // log.debug("Found \"" + name + "\" at \"" + found + "\"");
  //          return found;
  //      }
  //      
  //      static public void gunzip(String gzippedFile, String destFile) throws FileNotFoundException, IOException {
  //          int BUFFER = 2048;
  //          BufferedInputStream is = new BufferedInputStream(new GZIPInputStream(new FileInputStream(gzippedFile)));
  //          int currentByte;
  //          // establish buffer for writing file
  //          byte data[] = new byte[BUFFER];
  //
  //          // write the current file to disk
  //          FileOutputStream fos = new FileOutputStream(destFile);
  //          BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
  //
  //          // read and write until last byte is encountered
  //          while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
  //              dest.write(data, 0, currentByte);
  //          }
  //          dest.flush();
  //          dest.close();
  //          is.close();
  //      }

  // taken from http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
  static public void unZip(String zipFile) throws ZipException, IOException {
    System.out.println(zipFile);
    int BUFFER = 2048;
    File file = new File(zipFile);

    ZipFile zip = new ZipFile(file);
    String newPath = zipFile.substring(0, zipFile.length() - 4);

    new File(newPath).mkdir();
    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

    // Process each entry
    while (zipFileEntries.hasMoreElements()) {
      // grab a zip file entry
      ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
      String currentEntry = entry.getName();
      File destFile = new File(newPath, currentEntry);
      //destFile = new File(newPath, destFile.getName());
      File destinationParent = destFile.getParentFile();

      // create the parent directory structure if needed
      destinationParent.mkdirs();

      if (!entry.isDirectory()) {
        BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
        int currentByte;
        // establish buffer for writing file
        byte data[] = new byte[BUFFER];

        // write the current file to disk
        FileOutputStream fos = new FileOutputStream(destFile);
        BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

        // read and write until last byte is encountered
        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
          dest.write(data, 0, currentByte);
        }
        dest.flush();
        dest.close();
        is.close();
      }

      if (currentEntry.endsWith(".zip")) {
        // found a zip file, try to open
        unZip(destFile.getAbsolutePath());
      }
    }
  }

  /**
   * Create the specified zip file from the specified source files.  Code
   * adapted from http://www.java-examples.com/create-zip-file-multiple-files-using-zipoutputstream-example
   */
  public static void createZipFile(String prefix, String[] sourceFiles, String zipFile) {
    try {
      byte[] buffer = new byte[1024];
      FileOutputStream fout = new FileOutputStream(zipFile);
      ZipOutputStream zout = new ZipOutputStream(fout);
      for (int i = 0; i < sourceFiles.length; i++) {
        File f = new File(prefix + "/" + sourceFiles[i]);
        if (f.isDirectory()) {
          addDirectory(zout, f, f.getName());
        } else {
          FileInputStream fin = new FileInputStream(f);
          zout.putNextEntry(new ZipEntry(sourceFiles[i]));
          int length;
          while ((length = fin.read(buffer)) > 0) {
            zout.write(buffer, 0, length);
          }
          zout.closeEntry();
          fin.close();
        }
      }
      zout.close();
    } catch (IOException ioe) {
      throw new RuntimeException("createZipFile(\"" + prefix + "\",\"" + Arrays.asList(sourceFiles) + "\",\"" + zipFile + ")", ioe);
    }
  }

  private static void addDirectory(ZipOutputStream zout, File fileSource, String prefix) {
    File[] files = fileSource.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        addDirectory(zout, files[i], prefix + "/" + files[i].getName());
        continue;
      }
      try {
        byte[] buffer = new byte[1024];
        FileInputStream fin = new FileInputStream(files[i]);
        String entryName = prefix + "/" + files[i].getName();
        zout.putNextEntry(new ZipEntry(entryName));
        int length;
        while ((length = fin.read(buffer)) > 0) {
          zout.write(buffer, 0, length);
        }
        zout.closeEntry();
        fin.close();
      } catch (IOException ioe) {
        throw new RuntimeException("addDirectory(\"" + zout + "\",\"" + fileSource.getName() + "\",\"" + prefix + ")", ioe);
      }
    }
  }

  /**
   * Copies the lines in the source file into separate files in the destination dir, according to
   * the values in the 0-based column index specified.
   */
  public static void separateTsvFile(String sourceFile, String destDir, int index, boolean removeExisting, boolean append, boolean retainKey) {
    separateTsvFile(sourceFile, destDir, index, removeExisting, append, retainKey, null);
  }

  /**
   * Copies the lines in the source file into separate files in the destination dir, according to
   * the values in the 0-based column index specified.
   */
  public static void separateTsvFile(String sourceFile, String destDir, int index, boolean removeExisting, boolean append) {
    separateTsvFile(sourceFile, destDir, index, removeExisting, append, true, null);
  }

  /**
   * Copies the lines in the source file into separate files in the destination dir, according to
   * the values in the 0-based column index specified.
   */
  public static void separateTsvFile(String sourceFile, String destDir, int index, boolean removeExisting, boolean append, boolean retainKey,
      LineModifier lineModifier) {
    Map<String, BufferedWriter> outFiles = new HashMap<String, BufferedWriter>();
    try {
      File d = new File(destDir);
      if (removeExisting && d.exists() && d.isDirectory()) {
        for (File f : d.listFiles()) {
          f.delete();
        }
      }

      d.mkdirs();
      BufferedReader in = new BufferedReader(new FileReader(sourceFile));
      while (in.ready()) {
        String s = in.readLine().trim();
        if (s.length() == 0) continue;
        String[] parts = s.split("\\t");
        if (index >= parts.length) continue;
        String val = parts[index];
        BufferedWriter out = outFiles.get(val);
        if (out == null) {
          out = new BufferedWriter(new FileWriter(destDir + "/" + val, append));
          outFiles.put(val, out);
        }
        if (retainKey) {
          out.write((lineModifier != null) ? lineModifier.modify(s) : s);
        } else {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < parts.length; i++) {
            if (i != index) {
              if (sb.length() > 0) sb.append("\t");
              sb.append(parts[i]);
            }
          }
          out.write((lineModifier != null) ? lineModifier.modify(sb.toString()) : sb.toString());
        }
        out.write("\n");
      }
      in.close();
      for (BufferedWriter out : outFiles.values()) {
        out.close();
      }
    } catch (Exception e) {
      throw new RuntimeException("separateTsvFile(\"" + sourceFile + "\",\"" + destDir + "\",\"" + index + "\",\"" + removeExisting + "\",\""
          + append + "\",\"" + ")", e);
    }
  }

  public interface LineModifier {

    public String modify(String line);
  }
}
