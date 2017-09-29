// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: SampleScheduledJob 11/29/15 3:51 PM toures
*
* This computer code is copyright 2015 EMC Corporation.
* All rights reserved
*/

/**
 * Created with IntelliJ IDEA.
 * User: toures
 * Date: 11/29/15
 * Time: 3:51 PM
 */
public class SampleScheduledJob implements Runnable {
    private static int value = 0;

    @Override
    public void run() {
        value += 50;
    }

    public static int getValue() {
        return value;
    }
}
