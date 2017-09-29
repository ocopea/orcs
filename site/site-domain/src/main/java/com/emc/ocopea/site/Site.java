// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import com.emc.ocopea.site.artifact.SiteArtifactRegistry;
import com.emc.ocopea.site.copy.CopyRepository;
import com.emc.ocopea.site.dsb.Dsb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class Site {
    private static final Logger log = LoggerFactory.getLogger(Site.class);
    private final String name;
    private final Map<String, Dsb> dsbByUrn = new ConcurrentHashMap<>();
    private final Map<String, CopyRepository> copyRepositoryByUrn = new ConcurrentHashMap<>();
    private final Map<String, Psb> psbByUrn = new ConcurrentHashMap<>();
    private final Map<String, SiteArtifactRegistry> artifactRegistryByName = new ConcurrentHashMap<>();

    public Site(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /***
     * Add psb to the site.
     * @param psb psb to be added
     */
    public void addPSB(Psb psb) {
        // Adding, if already exists - so what overwriting..
        psbByUrn.put(psb.getUrn(), psb);
    }

    public Dsb addDSB(Dsb dsb) {
        dsbByUrn.put(dsb.getUrn(), dsb);
        return dsb;
    }

    public void removeDsb(String dsbUrn) {
        dsbByUrn.remove(dsbUrn);
    }

    public void addArtifactRegistry(SiteArtifactRegistry artifactRegistry) {
        artifactRegistryByName.put(artifactRegistry.getName(), artifactRegistry);
    }

    public void removeArtifactRegistry(String artifactRegistryName) {
        artifactRegistryByName.remove(artifactRegistryName);
    }

    public Collection<Dsb> getDsbList() {
        return new ArrayList<>(dsbByUrn.values());
    }

    public Dsb getDsb(String dsbURN) {
        return dsbByUrn.get(dsbURN);
    }

    /***
     * Get PSB URN by type.
     * @param psbType psb type
     */
    public String getPsbUrnByType(String psbType) {
        return
                psbByUrn.values()
                        .stream()
                        .filter(psb -> psb.getType().equalsIgnoreCase(psbType))
                        .map(Psb::getUrn)
                        .findFirst()
                        .orElse(null);
    }

    public Psb getPsb(String psbUrn) {
        return psbByUrn.get(psbUrn);
    }

    public CopyRepository getCopyRepositoryByUrn(String copyRepoUrn) {
        return copyRepositoryByUrn.get(copyRepoUrn);
    }

    public Collection<CopyRepository> getCopyRepositories() {
        return new ArrayList<>(copyRepositoryByUrn.values());
    }

    public Collection<SiteArtifactRegistry> getArtifactRegistires() {
        return new ArrayList<>(artifactRegistryByName.values());
    }

    public Collection<Psb> getPsbs() {
        return new ArrayList<>(psbByUrn.values());
    }

    public void addCopyRepository(CopyRepository copyRepository) {
        this.copyRepositoryByUrn.put(copyRepository.getUrn(), copyRepository);
    }

    public SiteArtifactRegistry getArtifactRegistry(String artifactRegistryName) {
        return artifactRegistryByName.get(artifactRegistryName);
    }

    public void removeCrb(String crbUrn) {
        copyRepositoryByUrn.remove(crbUrn);
    }
}
