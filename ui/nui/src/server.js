// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const path = require('path');
const express = require('express');
const fs = require('fs');
var url = require('url');

module.exports = {

    app: function () {
        const app = express();
        const indexPath = path.join(__dirname, '/../index.html');

        // API
        // ========

        const router = express.Router();
        // accessed at GET http://localhost:8080/hub-web-api
        router.get('/ws', function(req, res) {
          res.json({ message: 'fetch Websocket address' });
        });

        process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

        router.get('/world', function(req, res) {
            var domain = path.join(__dirname, "../public");

            fs.readFile( domain + "/appAvailabilityZone/world-110m.jsn", 'utf8', function (err, data) {
                res.json(JSON.parse(data))
                res.end( data );
            });
        });


        router.get('/app-template', function(req, res) {
            fs.readFile( __dirname + "/" + "js/data/_api/app.catalog.json", 'utf8', function (err, data) {
               res.end( data );
            });
        });

        app.use('http://localhost:9000/client-api', router);
        app.get('/', function (_, res) { res.sendFile(indexPath) });

        return app
    }
};
