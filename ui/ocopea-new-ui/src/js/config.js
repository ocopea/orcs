// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import DevTool, { configureDevtool } from 'mobx-react-devtools';

// Any configurations are optional
configureDevtool({
  // Turn on logging changes button programmatically:
  logEnabled: true,
  // Turn off displaying conponents' updates button programmatically:
  updatesEnabled: false,
  // Log only changes of type `reaction`
  // (only affects top-level messages in console, not inside groups)
  logFilter: change => change.type === 'reaction',
});
