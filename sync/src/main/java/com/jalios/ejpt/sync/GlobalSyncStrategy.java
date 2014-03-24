package com.jalios.ejpt.sync;

import java.util.LinkedList;
import java.util.List;

public class GlobalSyncStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport finalReport = new SyncStrategyReport();

    List<SyncStrategy> internalStrategies = new LinkedList<SyncStrategy>();
    internalStrategies.add(new FilesShoudBeDeclaredStrategy());
    internalStrategies.add(new FilesFromPluginXmlStrategy());
    internalStrategies.add(new NewFileFromWebappDirectoryStrategy());

    for (SyncStrategy syncStrategy : internalStrategies) {
      finalReport.mergeReport(syncStrategy.run(configuration));
    }

    return finalReport;
  }

}
