// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.inspector.InspectorMicroService;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.samples.bank.BankDBSchemaBootstrap;
import com.emc.microservice.samples.bank.BankUIServerMicroService;
import com.emc.microservice.samples.bank.DepositToAccountMicroService;
import com.emc.microservice.samples.calculator.CalculatorMicroService;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handmade code created by ohanaa Date: 2/26/15 Time: 11:07 AM
 */
public class SampleDevRunner {

    public static void main(String[] args) throws IOException, SQLException {
        runSample();
    }

    @NoJavadoc
    public static DevResourceProvider runSample() throws IOException, SQLException {
        Map<String, AbstractSchemaBootstrap> schemaBootstrapMap = new HashMap<>();
        schemaBootstrapMap.put(DepositToAccountMicroService.BANK_DB_NAME, new BankDBSchemaBootstrap());
        DevResourceProvider resourceProvider = new DevResourceProvider(schemaBootstrapMap);
        System.out.println(UUID.randomUUID().toString());
        new MicroServiceRunner().run(
                resourceProvider,
                new InspectorMicroService(),
                new CalculatorMicroService(),
                new DepositToAccountMicroService(),
                new BankUIServerMicroService());

        return resourceProvider;
    }
}
