// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import com.emc.ocopea.site.SupportedServiceDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liebea on 8/7/16.
 * Drink responsibly
 */
public class DsbResolver {
    private final Supplier<Collection<SupportedServiceDto>> catalogSupplier;
    private final String siteName;
    private final Map<String, List<ProtocolDsbMatch>> protocolsToDsbs;

    public static class ProtocolDsbMatch {
        private final SupportedServiceDto service;
        private final SupportedServiceDto.SupportedServiceProtocolDto protocol;
        private final SupportedServiceDto.SupportedServicePlanDto plan;

        public ProtocolDsbMatch(
                SupportedServiceDto service,
                SupportedServiceDto.SupportedServiceProtocolDto protocol,
                SupportedServiceDto.SupportedServicePlanDto plan) {
            this.service = service;
            this.protocol = protocol;
            this.plan = plan;
        }

        public SupportedServiceDto getService() {
            return service;
        }

        public SupportedServiceDto.SupportedServiceProtocolDto getProtocol() {
            return protocol;
        }

        public SupportedServiceDto.SupportedServicePlanDto getPlan() {
            return plan;
        }
    }

    public DsbResolver(
            Supplier<Collection<SupportedServiceDto>> catalogSupplier,
            String siteName) {
        this.catalogSupplier = catalogSupplier;
        this.siteName = siteName;

        this.protocolsToDsbs = getMatchingCache();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Listing supported DSBs by a site for list of protocols required by a service
     */
    public Collection<ProtocolDsbMatch> listDSBs(Collection<AppServiceExternalDependencyProtocol> protocols) {
        // Getting a DSB that supports the requested protocol
        return findDsbs(
                null,
                protocols
                        .stream()
                        .map(p -> protocolsToDsbs.getOrDefault(p.getProtocol(), Collections.emptyList()))
                        .filter(l -> !l.isEmpty())
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public Collection<ProtocolDsbMatch> listDSBs(AppServiceExternalDependencyProtocol protocol) {
        return listDSBs(Collections.singleton(protocol));
    }

    public String resolveDsbForDependency(AppServiceExternalDependencyProtocol protocol) {
        return resolveDsbForDependency(protocol, null);
    }

    /**
     * Searching for a known DSB supporting required abilities. Currently DSBs can be searched by protocol and the
     * ability to restore from a copies made by a given DSB.
     *
     * @param protocol data service protocol the DSB must support
     * @param mustSupportRestoreFromDSB identifier of a given DSB, the returned DSB must support restoring from copies
     *        made by this DSB. DO NOT USE. not yet implemented.
     *
     * @return URN of a dsb meeting the requirements
     *
     * @throws IllegalStateException if no appropriate DSB found
     */
    // TODO: is ISE the correct exception?
    public String resolveDsbForDependency(
            AppServiceExternalDependencyProtocol protocol,
            String mustSupportRestoreFromDSB) {

        // Getting a DSB that supports the requested protocol
        final List<ProtocolDsbMatch> pairs = protocolsToDsbs.get(protocol.getProtocol());
        if (pairs == null) {
            throw new IllegalStateException("Could not find a DSB implementing protocol " + protocol.getProtocol()
                    + " on site " + siteName);
        }
        final Optional<ProtocolDsbMatch> first = findDsbs(mustSupportRestoreFromDSB, pairs).findFirst();

        if (!first.isPresent()) {
            throw new IllegalStateException("Could not find a DSB implementing protocol " + protocol.getProtocol()
                    + " on site " + siteName + " that apply to all app protocol conditions");
        }
        return first.get().getService().getUrn();
    }

    // TODO: consider renaming this method.
    private Stream<ProtocolDsbMatch> findDsbs(
            String mustSupportRestoreFromDsb,
            List<ProtocolDsbMatch> matches) {
        return matches.stream().filter(p -> {
            // If restoring from copy, need to support the original copy
            if (mustSupportRestoreFromDsb != null) {
                //todo: 1)comparing by name is wrong I think since I think one is urn and other is name..
                //todo: 2) need a better way to understand whether we can convert data between two DSBs,
                // todo-continuation: probably pass the facility too and copy additional params.....
                return p.getService().getUrn().equals(mustSupportRestoreFromDsb);
            }
            //todo: implement filtering according to conditions and version!
            return true;
        });
    }

    private Map<String, List<ProtocolDsbMatch>> getMatchingCache() {
        // Mapping between protocols to DSB
        List<ProtocolDsbMatch> dsbMatchList = new ArrayList<>();

        getCatalogFromSite().forEach(
                s -> s.getPlans().forEach(plan -> plan.getSupportedProtocols().forEach(p ->
                        dsbMatchList.add(new ProtocolDsbMatch(s, p, plan)))));

        return dsbMatchList.stream()
                .collect(Collectors.groupingBy(o -> o.getProtocol().getProtocolName()));
    }

    private Collection<SupportedServiceDto> getCatalogFromSite() {
        return HubWebAppUtil.wrap(
                "Getting DSB catalog from site",
                catalogSupplier);
    }
}
