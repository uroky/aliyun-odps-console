/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.aliyun.openservices.odps.console.pub;

import org.junit.Assert;
import org.junit.Test;

import com.aliyun.odps.OdpsException;
import com.aliyun.openservices.odps.console.ExecutionContext;
import com.aliyun.openservices.odps.console.ODPSConsoleException;

public class ShowTablesCommandTest {

  private static String[] positives = {"SHOW TABLES", "show tables", " show tables \r",
                                       "show tables in project01_",
                                       "\n\r\t SHow\tTables  in \r project_01_sdf\n\r\t"};

  private static String[] negatives = {"show", "show table", "show tables in ", "show partition"};

  private static String[] pubPositives = {"LS TABLES", "ls tables", " ls tables \r",
                                          "ls tables -project=project01_",
                                          "\n\r\t Ls\tTables  -project=project_01_sdf\n\r\t",
                                          "LIST TABLES", "list tables", " list tables \r",
                                          "list tables -project=project01_",
                                          "\n\r\t LIsT\tTables  -project=project_01_sdf\n\r\t"};

  private static String[]
      pubNegatives =
      {"ls", "ls table", "ls partition", "list", "list table", "list partition"};

  private static String[]
      pubMatchProjects =
      {"ls tables -p project_test", "ls tables -project project_test",
       "ls tables -project=project_test", "ls tables -p=project_test",
       "list tables -p project_test", "list tables -project project_test",
       "list tables -project=project_test", "list tables -p=project_test"};

  private static String[]
      pubLackPara =
      {"ls tables -p", "ls tables -project", "list tables -p", "list tables -project"};

  private static String[] pubInvalidPara = {"ls tables xxx", "ls tables -xxx"};

  private static String[] prefixPositives = {"save%", "%", "jdbc%", "temp*", "abc*"};

  private static String[] prefixNegatives = {"!!!%"};

  private static String[] prefixMatchPositives = {"show tables in project_01_sdf like '%s'",
                                                  "show tables like '%s'",
                                                  "ShOw TAbLEs LIkE '%s'"};

  private static String[] prefixMatchNegatives = {"show table in project_01_sdf like '%s'",
                                                  "show tables in project_01_sdf like %s",
                                                  "show tables in project_01_sdf '%s'",
                                                  "show tables in project_01_sdf %s'",
                                                  "show tables in like '%s'"};

  @Test
  public void testMatchPositive() throws ODPSConsoleException, OdpsException {
    ExecutionContext ctx = ExecutionContext.init();
    for (String cmd : positives) {
      // all positive commands should match internal regex pattern
      Assert.assertTrue(ShowTablesCommand.matchInternalCmd(cmd).matches());

      // all positive commands HERE should have NULL prefix name
      Assert.assertNull(ShowTablesCommand.getPrefixName(ShowTablesCommand.matchInternalCmd(cmd)));
    }

    ShowTablesCommand command = ShowTablesCommand.parse("show tables", ctx);
    Assert.assertNotNull(command);
    command.run();
  }


  @Test
  public void testMatchNegative() {
    for (String cmd : negatives) {
      Assert.assertFalse(ShowTablesCommand.matchInternalCmd(cmd).matches());
    }
  }

  @Test
  public void testMatchGroup() {
    String cmd = "show tables in project";
    Assert.assertEquals("project",
                        ShowTablesCommand.getProjectName(ShowTablesCommand.matchInternalCmd(cmd)));
  }

  @Test
  public void testMatchPublicPositive() {
    for (String cmd : pubPositives) {
      Assert.assertTrue(ShowTablesCommand.matchPublicCmd(cmd).matches());
    }
  }

  @Test
  public void testMatchPublicNegative() {
    for (String cmd : pubNegatives) {
      Assert.assertFalse(ShowTablesCommand.matchPublicCmd(cmd).matches());
    }
  }

  @Test
  public void testLackProjectName() throws ODPSConsoleException {
    ExecutionContext context = ExecutionContext.init();

    for (String cmd : pubLackPara) {
      try {
        ShowTablesCommand command = ShowTablesCommand.parse(cmd, context);
        Assert.assertNotNull(null);
      } catch (ODPSConsoleException e) {

      }
    }
  }

  @Test
  public void testMatchPublicGroup() throws ODPSConsoleException, OdpsException {
    ExecutionContext context = ExecutionContext.init();

    for (String cmd : pubMatchProjects) {
      ShowTablesCommand command = ShowTablesCommand.parse(cmd, context);
      Assert.assertEquals("project_test", command.getProjectNameFromPublicCmd(cmd));
    }

    String cmd2 = "ls tables";

    ShowTablesCommand command2 = ShowTablesCommand.parse(cmd2, context);
    Assert.assertNull(command2.getProjectNameFromPublicCmd(cmd2));
    command2.run();
  }

  @Test
  public void testInvalidParams() throws ODPSConsoleException, OdpsException {
    ExecutionContext context = ExecutionContext.init();

    for (String cmd : pubInvalidPara) {
      try {
        ShowTablesCommand command = ShowTablesCommand.parse(cmd, context);
        Assert.assertNotNull(null);
      } catch (ODPSConsoleException e) {

      }
    }
  }

  @Test
  public void testPrefixPositives() throws ODPSConsoleException, OdpsException {
    ExecutionContext context = ExecutionContext.init();

    for (String cmd : prefixMatchPositives) {
      for (String prefix : prefixPositives) {
        String cmdStr = String.format(cmd, prefix);
        ShowTablesCommand command = ShowTablesCommand.parse(cmdStr, context);
        Assert.assertNotNull(command);
        String actualPrefix = prefix.substring(0, prefix.length() - 1);
        Assert.assertEquals(actualPrefix,
                  ShowTablesCommand.getPrefixName(ShowTablesCommand.matchInternalCmd(cmdStr)));
      }
    }

    String cmd = "show tables like '%s'";
    for (String prefix : prefixPositives) {
      ShowTablesCommand commandPositive = ShowTablesCommand.parse(String.format(cmd, prefix), context);
      commandPositive.run();
    }
  }

  @Test
  public void testPrefixNegatives() throws ODPSConsoleException {
    ExecutionContext context = ExecutionContext.init();

    for (String cmd : prefixMatchNegatives) {
      for (String prefix : prefixNegatives) {
        String cmdStr = String.format(cmd, prefix);
        ShowTablesCommand command = ShowTablesCommand.parse(cmdStr, context);
        Assert.assertNull(command);
      }
    }
  }

}
