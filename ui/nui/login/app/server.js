// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const path = require('path');
const express = require('express');
const fs = require('fs');

module.exports = {
    app: function () {
        const app = express();
        const indexPath = path.join(__dirname, 'index.html');
                
        // API
        // ========
         
        const router = express.Router();  
        // accessed at GET http://localhost:8080/hub-web-api
        router.get('/', function(req, res) {
            res.json({ message: 'Nazgul API' });   
        });
        
        router.get('/app-template', function(req, res) {
            fs.readFile( __dirname + "/" + "js/data/_api/app.catalog.json", 'utf8', function (err, data) {
               res.end( data );
            });
        });                      
        
        app.use('/hub-web-api', router);
        app.get('/', function (_, res) { res.sendFile(indexPath) });

        return app
    }
};


