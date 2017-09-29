// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var express = require('express');
const path = require('path');
var app = express();
const indexPath = path.join(__dirname, 'index.html');
const publicPath = express.static(path.join(__dirname, 'build'));

app.set('port', (process.env.PORT || 8080));

app.use(express.static(__dirname + '/build'));

// views is directory for all template files
//app.set('views', __dirname)


app.set('view engine', 'ejs');

app.get('/', function(request, response) {
    console.log(response)
  response.render('index');
});

app.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});

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
app.use('/public', publicPath);
app.get('/', function (_, res) { res.sendFile(indexPath) });
