/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.zosfile;

import java.util.Map;

/**
 * Representation of a UNIX file or directory.
 *
 */
public interface IZosUNIXFile {
    
    /**
     * Enumeration of data type for store and retrieve of data set content:
     * <li>{@link #TEXT}</li>
     * <li>{@link #BINARY}</li>
     */
    public enum UNIXFileDataType {
        /**
         * Content is between ISO8859-1 on the client and EBCDIC on the host
         */
        TEXT("text"),
        /**
         * No data conversion is performed
         */
        BINARY("binary");
        
        private String dataType;
        
        UNIXFileDataType(String dataType) {
            this.dataType = dataType;
        }
        
        @Override
        public String toString() {
            return dataType;
        }
    }
    
    /**
     * Create the zOS UNIX file or directory. Will be deleted at test method end
     * @return
     * @throws ZoException 
     */
    public IZosUNIXFile create() throws ZosUNIXFileException;

    /**
     * Delete the zOS UNIX file or directory from the zOS image. Attempting to delete a non-empty directory will throw {@link ZosUNIXFileException}
     * @return deleted
     * @throws ZosUNIXFileException
     */
    public boolean delete() throws ZosUNIXFileException;
    
    /**
     * Recursively delete the zOS UNIX directory and its contents from the zOS image
     * @return deleted
     * @throws ZosUNIXFileException
     */
    public boolean directoryDeleteNonEmpty() throws ZosUNIXFileException;

    /**
     * Return true if the zOS UNIX exists on the zOS image
     * @return
     * @throws ZosUNIXFileException
     */
    public boolean exists() throws ZosUNIXFileException;

    /**
     * Write the content to the zOS UNIX file on the zOS image. Data type is can be set by {@link #setDataType(UNIXFileDataType)}
     */
    public void store(String content) throws ZosUNIXFileException;

    /**
     * Retrieve the content of the zOS UNIX file from the zOS image. Data type is can be set by {@link #setDataType(UNIXFileDataType)}
     * @throws ZosUNIXFileException
     */
    public String retrieve() throws ZosUNIXFileException;

    /**
     * Recursively store the content of the zOS UNIX file or directory to the Results Archive Store
     * @param rasPath path in Results Archive Store
     * @throws ZosUNIXFileException
     */
    public void saveToResultsArchive(String rasPath) throws ZosUNIXFileException;
    
    /**
     * Return true if this object represents a zOS UNIX directory
     * @return
     * @throws ZosUNIXFileException
     */
    public boolean isDirectory() throws ZosUNIXFileException;
    
    /**
     * Returns sorted {@link Map} the zOS UNIX files and directories in this zOS UNIX directory
     * @return
     * @throws ZosUNIXFileException
     */
    public Map<String, String> directoryList() throws ZosUNIXFileException;
    
    /**
     * Returns recursive sorted {@link Map} the zOS UNIX files and directories in this zOS UNIX directory
     * @return
     * @throws ZosUNIXFileException
     */
    public Map<String, String> directoryListRecursive() throws ZosUNIXFileException;
    
    /**
     * Set the data type ({@link UNIXFileDataType}) for store and retrieve of the zOS UNIX file content
     * @param dataType
     */
    public void setDataType(UNIXFileDataType dataType);
    
    /**
     * Return the data type ({@link UNIXFileDataType}) for store and retrieve of the zOS UNIX file content
     * @param dataType
     */
    public UNIXFileDataType getDataType();
    
    /**
     * Return the path of the zOS UNIX file or directory
     * @param dataType
     */
    public String getUnixPath();
    
    /**
     * Return the file name of the zOS UNIX file, or null if this object represents a directory 
     * @return
     */
    public String getFileName();

    /**
     * Get the directory path for the zOS UNIX file or directory
     * @return
     */
    public String getDirectoryPath();

    /**
     * Return the attributes of the zOS UNIX file or directory as a {@link String} 
     * @return
     * @throws ZosUNIXFileException
     */
    public String getAttributesAsString() throws ZosUNIXFileException;

    /**
     * Set flag to control if the content of the zOS UNIX path should be stored to the test output. Defaults to false
     */    
    public void setShouldArchive(boolean shouldArchive);

    /**
     * Return flag that controls if the content of the zOS UNIX path should be stored to the test output
     */    
    public boolean shouldArchive();

    /**
     * Set flag to control if the zOS UNIX path should be automatically deleted from zOS at test end. Defaults to true
     */    
    public void setShouldCleanup(boolean shouldCleanup);

    /**
     * Return flag that controls if the zOS UNIX path should be automatically deleted from zOS at test end
     */    
    public boolean shouldCleanup();
}
