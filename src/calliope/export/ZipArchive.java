/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calliope.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Turn a folder into a zip file
 * @author desmond
 */
public class ZipArchive implements Compressor
{
    File srcFolder;
    byte[] zipBuf = new byte[4096];
    public ZipArchive( File archive )
    {
        this.srcFolder = archive;
    }
    /**
     * Zip a folder  
     * @param src the source folder to zip up
     * @param dest the destination zip file
     */
    private void zipFolder( String src, String dest ) throws Exception
    {
        FileOutputStream fw = new FileOutputStream( dest );
        ZipOutputStream zip = new ZipOutputStream( fw );
        addFolderToZip( "", src, zip );
        zip.flush();
        zip.close();
    }
    /**
     * Recursively add files to the zip files
     * @param path path of file to include
     * @param srcFile
     * @param zip 
     * @param flag
     */
    private void addFileToZip( String path, String srcFile, 
        ZipOutputStream zip, boolean flag ) throws Exception 
    {
        File folder = new File( srcFile );
        if ( flag )
            zip.putNextEntry( new ZipEntry(path+"/"+folder.getName()+"/") );
        else if (folder.isDirectory() ) 
            addFolderToZip( path, srcFile, zip );
        else
        {
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(zipBuf)) > 0) 
                zip.write(zipBuf, 0, len);
        }
    }
    /**
     * Add folder to the zip file 
     * @param path
     * @param srcFolder
     * @param zip the zip stream to write to
     */
    private void addFolderToZip( String path, String srcFolder, 
        ZipOutputStream zip ) throws Exception 
    {
        File folder = new File(srcFolder);
        if ( folder.list().length == 0 )
            addFileToZip(path , srcFolder, zip, true );
        else
        {
            for (String fileName : folder.list())
            {
                if (path.equals(""))
                    addFileToZip(folder.getName(), srcFolder + "/" 
                        +fileName, zip,false);
                else
                    addFileToZip(path + "/" + folder.getName(), srcFolder 
                        + "/" + fileName, zip,false);
            }
        }
    }
    /**
     * Convert a folder to a zip archive
     * @return a zip file reference
     */
    @Override
    public File compress() throws Exception
    {
        File dest = File.createTempFile( "", "" );
        zipFolder( srcFolder.getAbsolutePath(), 
            dest.getAbsolutePath() );
        return dest;
    }
}
