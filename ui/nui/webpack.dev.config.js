// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

console.log("develpement")


module.exports = {
    devtool: 'source-map',

    entry: [
        'babel-polyfill',
        'webpack/hot/dev-server',
        'webpack-hot-middleware/client',
        './src/index'
    ],

    output: {
        path: '/',
        filename: 'bundle.js',
        publicPath: 'http://localhost:8080/public/'
    },

    plugins: [
      new webpack.HotModuleReplacementPlugin(),
      new webpack.NoErrorsPlugin(),
      new ExtractTextPlugin('./css/style.css', { allChunks: true }),
      new HtmlWebpackPlugin({
        favicon: './src/module-development/assets/favicon.png'
      }),
  		new webpack.DefinePlugin({
        // http://liebea-orcs.cf.isus.emc.com
  			APISERVER: JSON.stringify("http://localhost:8083"),
  			PUBLICDOMAIN: JSON.stringify("public/"),
  			SANKEYDOMAIN: JSON.stringify("public/sankey/")
  		})
    ],

    module: {
        loaders: [
            {
              test: /\.json?$/,
              loader: 'json'
            },
            {
              test: /\.(js|jsx)?$/,
              loader: 'babel',
              exclude: [
                  /node_modules/,
                  path.join(__dirname, 'src/module-production/js/sankey'),
                  path.join(__dirname, 'src/module-production/js/data/world-country-names.tsv'),
                  path.join(__dirname, 'src/module-production/js/data/world-110m.json')
              ],
              query: {
                plugins: [
                  'transform-runtime',
                  'transform-decorators-legacy',
                  'transform-object-rest-spread',
                  'transform-class-properties'],
                presets: ['es2015', 'react']
              }
            },
            {
              test: /\.sass$/,
              loader: ExtractTextPlugin.extract('css-loader?sourceMap!autoprefixer-loader?{browsers:["last 2 version"]}!sass-loader?indentedSyntax&sourceMap&sourceMapContents')
            },
            {
              test: /\.scss$/,
              loader: ExtractTextPlugin.extract('css!autoprefixer-loader!sass')
            },
            { test: /\.css$/, loader: "style-loader!css-loader" },
      			{
      				test: /\.(jpe?g|gif|png|ttf|eot|svg|woff|woff2)[\?]?.*$/,
      				loader: 'url-loader?limit=10000&name=assets/[name].[ext]'
      			}
        ]
    }
}
