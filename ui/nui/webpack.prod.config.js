// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');


//APPLY LINE BELOW BEFORE PRODUCTION
//process.env.NODE_ENV = 'production';

console.log("production")

module.exports = {
    devtool: 'source-map',

    entry: [
        'babel-polyfill',
      	'./src/index'
    ],

    output: {
        path: path.join(__dirname, 'public'),
        filename: 'bundle.js',
        publicPath: ''
    },

    plugins: [
      new webpack.HotModuleReplacementPlugin(),
      new webpack.NoErrorsPlugin(),
      new ExtractTextPlugin('./style.css', { allChunks: true }),
      new HtmlWebpackPlugin({
        title: 'Project Nazgul',
        template: './src/template.html',
        favicon: './src/module-development/assets/favicon.png'
      }),

  		new webpack.DefinePlugin({
  			APISERVER: JSON.stringify(""),
  			PUBLICDOMAIN: JSON.stringify(""),
  			SANKEYDOMAIN: JSON.stringify("sankey/")
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
                presets: ['es2015', 'react'],
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
      				test: /\.jpe?g$|\.gif$|\.png$|\.wav$|\.svg$|\.mp3$/,
      				loader: 'file-loader?limit=10000&name=assets/[name].[ext]'
      			},
            {
              test: /\.(ttf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
              loader: 'file?limit=10000&name=assets/[name].[ext]'
            }
        ]
    }
}
