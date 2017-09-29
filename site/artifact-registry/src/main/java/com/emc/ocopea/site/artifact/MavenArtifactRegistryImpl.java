// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.artifact;

import com.emc.microservice.webclient.WebAPIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by liebea on 1/15/17.
 * Drink responsibly
 */
public class MavenArtifactRegistryImpl implements ArtifactRegistryApi {
    private static final Logger log = LoggerFactory.getLogger(MavenArtifactRegistryImpl.class);
    private final WebAPIResolver webAPIResolver;
    private final String url;

    public MavenArtifactRegistryImpl(WebAPIResolver webAPIResolver, String url) {
        this.webAPIResolver = webAPIResolver;
        this.url = url;
    }

    /**
     * @param artifactId must be of the form  'groupId:artifactId'. It may also contain additional terms, such as
     *        'groupId:artifactId:type'. These additional terms are ignored.
     *
     * @return A list of available versions for the given artifact.
     *
     * @throws IllegalArgumentException if artifactId is malformed or downloading the metadata file failed.
     * @throws IllegalStateException if failed reading versions from the downloaded maven metadata file
     */
    @Override
    public Collection<String> listVersions(String artifactId) {
        final String[] artifactParts = artifactId.split(":");
        if (artifactParts.length < 2) {
            throw new IllegalArgumentException("artifactId='" + artifactId + "' must contain '<groupId>:<artifactId>'");
        }
        final String groupId = artifactParts[0];
        final String artifact = artifactParts[1];

        XPath xpath = XPathFactory.newInstance().newXPath();
        final String expression = "/metadata/versioning/versions/version";

        NodeList nodes;
        try (InputStream mavenMetaData = downloadArtifactMetaData(groupId, artifact)) {
            InputSource inputSource = new InputSource(mavenMetaData);
            nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
        } catch (IOException e) {
            throw new IllegalArgumentException("failed downloading meta-data file for groupId=" + groupId +
                    " artifact=" + artifact, e);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("failed reading versions from maven meta-data for groupId=" +
                    groupId + " artifact=" + artifact, e);
        }

        List<String> versions = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {  // seriously...
            versions.add(0, nodes.item(i).getTextContent());
        }
        return versions;
    }

    private InputStream downloadArtifactMetaData(String groupId, String artifactId) {
        log.debug("fetching artifact versions for groupId {}; artifactId {}; using url {}", groupId, artifactId, url);
        try {
            String metadataUrl = url;
            if (!metadataUrl.endsWith("/")) {
                metadataUrl += '/';
            }
            metadataUrl += groupId.replaceAll("\\.", "/") + "/" + artifactId + "/maven-metadata.xml";
            log.debug("Getting maven metadata from {}", metadataUrl);
            return webAPIResolver.getWebTarget(metadataUrl).request().get().readEntity(InputStream.class);
        } catch (Exception ex) {
            log.debug("failed fetching artifact versions for groupId {}; artifactId {} using url {}",
                    groupId, artifactId, url);

            // Trace with exception...
            log.trace("failed fetching artifact versions for groupId " + groupId + "; artifactId " + artifactId +
                    "; using url " + url, ex);
            throw ex;
        }
    }
}
