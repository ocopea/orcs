// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const Server = require('./server.js')
const express = require('express');
const port = (process.env.PORT || 8080)
const app = Server.app()


if (process.env.NODE_ENV !== 'production') {
    const webpack = require('webpack')
    const webpackDevMiddleware = require('webpack-dev-middleware')
    const webpackHotMiddleware = require('webpack-hot-middleware')

    const config = require('../webpack.dev.config.js')
    const compiler = webpack(config)

    app.use(webpackHotMiddleware(compiler))
    app.use(webpackDevMiddleware(compiler, {
        noInfo: false,
        publicPath: config.output.publicPath,
        stats: {colors: true},
        log: console.log
    }));
}
else{
    var publicPath = express.static(path.join(__dirname, '../public'));
    app.use('/public', publicPath);
}

app.listen(port)
//console.log(`Listening at http://localhost:${port}`)
