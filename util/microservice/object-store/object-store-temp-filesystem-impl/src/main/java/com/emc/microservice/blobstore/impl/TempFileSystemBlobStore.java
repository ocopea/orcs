// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * This computer code is copyright 2014 EMC Corporation.
 * All rights reserved
 */
package com.emc.microservice.blobstore.impl;

import com.emc.microservice.blobstore.BlobReader;
import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.blobstore.BlobWriter;
import com.emc.microservice.blobstore.DuplicateObjectKeyException;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.blobstore.ObjectKeyFormatException;
import com.emc.ocopea.util.io.StreamUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Blob Store Implementation using temporary files as  a storage. Files and all information would be removed as soon as
 * JVM running this implementation stopped. Create method would not try to look over file system, so the get/read
 * methods will return back first found record. Update method will just reuse create method and Delete will delete
 * everything for namespace/key In case if Namespace + Key overlaps with another one e.g.
 * {namespace:"MyUnique",key:"Image"} and {namespace:"My",key:"UniqueImage"} they will be the same for this blobstore
 * service implementation.
 */
public class TempFileSystemBlobStore implements BlobStore {

    private static final Logger log = LoggerFactory.getLogger(TempFileSystemBlobStore.class);

    private static final String HEADERS_PREFIX = "header";
    private static final String BLOB_PREFIX = "blob";
    private static final String KEY_ACCEPTED_CHARS = "([\\p{Alnum}_-])*";
    private static final Pattern ACCEPTED_CHARS_PATTERN = Pattern.compile(KEY_ACCEPTED_CHARS);

    private final File folder;

    public TempFileSystemBlobStore() {
        try {
            folder = Files.createTempDirectory(BLOB_PREFIX).toFile();
        } catch (IOException err) {
            throw new IllegalStateException("Unable to create temporary folder");
        }
    }

    protected static void isKeyAcceptable(String key) throws ObjectKeyFormatException {
        if (!ACCEPTED_CHARS_PATTERN.matcher(key).matches()) {
            throw new ObjectKeyFormatException("Key '" + key + "' doesn't match pattern.");
        }
    }

    public void create(String namespace, String key, Map<String, String> headers, final InputStream blob)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        if (blob != null) {
            create(namespace, key, headers, out -> StreamUtil.copy(blob, out));
        } else {
            create(namespace, key, headers, (BlobWriter) null);
        }
    }

    @Override
    public void create(String namespace, String key, Map<String, String> headers, BlobWriter blobWriter)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        if (isExists(namespace, key)) {
            throw new DuplicateObjectKeyException("key already exist: " + namespace + "/" + key);
        }
        isKeyAcceptable(key);
        isKeyAcceptable(namespace);
        try {
            if (headers != null) {
                File headersFile = File.createTempFile(HEADERS_PREFIX + namespace + "." + key + ".", ".tmp", folder);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(headersFile, headers);
                headersFile.deleteOnExit();
            }
            if (blobWriter != null) {
                File blobFile = File.createTempFile(BLOB_PREFIX + namespace + "." + key + ".", ".tmp", folder);
                try (FileOutputStream fos = new FileOutputStream(blobFile)) {
                    blobWriter.write(fos);
                    fos.flush();
                }
                blobFile.deleteOnExit();
            }
        } catch (IOException ioe) {
            log.error("IOException happened", ioe);
            throw new IllegalStoreStateException("IOException happened", ioe);
        }
    }

    @Override
    public void update(String namespace, String key, Map<String, String> headers, InputStream blob)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        try {
            delete(namespace, key);
            create(namespace, key, headers, blob);
        } catch (DuplicateObjectKeyException e) {
            log.error(
                    "Duplicate keys happened. most likely something wrong with implementation. " +
                            "This should never happens.",
                    e);
        }
    }

    @Override
    public void update(String namespace, String key, Map<String, String> headers, BlobWriter blobWriter)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        try {
            delete(namespace, key);
            create(namespace, key, headers, blobWriter);
        } catch (DuplicateObjectKeyException e) {
            log.error(
                    "Duplicate keys happened. most likely something wrong with implementation. " +
                            "This should never happens.",
                    e);
        }
    }

    @Override
    public void moveNameSpace(final String oldNamespace, final String key, final String newNamespace)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        isKeyAcceptable(key);
        isKeyAcceptable(oldNamespace);
        isKeyAcceptable(newNamespace);

        try {
            File[] files = folder.listFiles(getHeadersFilenameFilter(oldNamespace, key));
            ren(files, HEADERS_PREFIX + oldNamespace + "." + key, HEADERS_PREFIX + newNamespace + "." + key + ".");
            files = folder.listFiles(getFilenameFilter(oldNamespace, key));
            ren(files, BLOB_PREFIX + oldNamespace + "." + key, BLOB_PREFIX + newNamespace + "." + key + ".");

        } catch (IOException e) {
            throw new IllegalStoreStateException("IOException happened", e);
        }
    }

    /***
     * Rename namespace
     */
    public void ren(File[] files, String target, String replacement) throws IOException {
        if (files != null) {
            for (File f : files) {
                String fileName = f.getName();
                String newFileName = fileName.replace(target, replacement);
                rename(f, newFileName);

            }
        }
    }

    private void rename(File file, String newFileName) throws IOException {
        File parent = file.getParentFile();
        file.renameTo(new File(parent, newFileName));
    }

    @Override
    public void readBlob(final String namespace, final String key, BlobReader reader)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        isKeyAcceptable(key);
        isKeyAcceptable(namespace);
        try {
            File[] files = getBlobFiles(namespace, key);
            if (files != null && files.length > 0) {
                try (FileInputStream fis = new FileInputStream(files[0])) {
                    reader.read(fis);
                }
            } else {
                throw new IllegalArgumentException("Key not found in store " + namespace + " : " + key);
            }

        } catch (IOException e) {
            throw new IllegalStoreStateException("IOException happened", e);
        }

    }

    @Override
    public void readBlob(final String namespace, final String key, final OutputStream out)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        readBlob(namespace, key, in -> StreamUtil.copy(in, out));

    }

    @Override
    public Map<String, String> readHeaders(final String namespace, final String key)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        isKeyAcceptable(key);
        isKeyAcceptable(namespace);
        Map<String, String> response = Collections.emptyMap();
        try {
            File[] files = folder.listFiles(getHeadersFilenameFilter(namespace, key));
            if (files != null && files.length > 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                response = objectMapper.readValue(files[0], new TypeReference<HashMap<String, String>>() {
                });
            }
        } catch (IOException e) {
            log.debug("IOException happened", e);
        }
        return response;
    }

    @Override
    public void delete(int expirySeconds) throws IllegalStoreStateException {
    }

    @Override
    public void delete(final String namespace, final String key)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        isKeyAcceptable(key);
        isKeyAcceptable(namespace);
        File[] files = folder.listFiles(getHeadersFilenameFilter(namespace, key));
        del(files);
        files = folder.listFiles(getFilenameFilter(namespace, key));
        del(files);
    }

    public FilenameFilter getHeadersFilenameFilter(String namespace, String key) {
        return (dir, name) -> name.startsWith(HEADERS_PREFIX + namespace + "." + key + ".");
    }

    public FilenameFilter getFilenameFilter(String namespace, String key) {
        return (dir, name) -> name.startsWith(BLOB_PREFIX + namespace + "." + key + ".");
    }

    private void del(File[] files) {
        if (files != null) {
            for (File f : files) {
                if (!f.delete()) {
                    log.warn("Unable to delete the file " + f.getName());
                }
            }
        }
    }

    @Override
    public boolean isExists(final String namespace, final String key) throws IllegalStoreStateException {
        try {
            isKeyAcceptable(key);
            isKeyAcceptable(namespace);
        } catch (ObjectKeyFormatException e) {
            return false;
        }

        try {
            File[] files = getBlobFiles(namespace, key);
            return (files != null && files.length > 0);
        } catch (IOException e) {
            return false;
        }
    }

    private File[] getBlobFiles(final String namespace, final String key) throws IOException {
        return folder.listFiles(getFilenameFilter(namespace, key));
    }

    @Override
    public Collection<BlobStoreLink> list() {
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        List<BlobStoreLink> linkList = new ArrayList<>(files.length);
        for (File currFile : files) {
            String name = currFile.getName();
            if (name.startsWith(BLOB_PREFIX)) {
                String[] split = name.substring(BLOB_PREFIX.length()).split("\\.");
                linkList.add(new BlobStoreLink(split[0], split[1]));
            }
        }
        return linkList;
    }

}
