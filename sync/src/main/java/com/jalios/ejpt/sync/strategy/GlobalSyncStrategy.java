/*
 GNU LESSER GENERAL PUBLIC LICENSE
 Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


 This version of the GNU Lesser General Public License incorporates
 the terms and conditions of version 3 of the GNU General Public
 License
 */
package com.jalios.ejpt.sync.strategy;

import java.util.LinkedList;
import java.util.List;

import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public class GlobalSyncStrategy implements SyncStrategy {

  @Override
  public SyncReportManager run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncReportManager reportManager = new SyncReportManager();

    List<SyncStrategy> strategies = new LinkedList<SyncStrategy>();
    strategies.add(new FilesShoudBeDeclared());
    strategies.add(new FilesExpectedByPluginXml());
    strategies.add(new FilesCouldBeMissed());

    for (SyncStrategy strategy : strategies) {
      reportManager.mergeReport(strategy.run(configuration));
    }

    return reportManager;
  }

}
