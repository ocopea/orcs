// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
'use strict';

var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');
var StatsPlugin = require('stats-webpack-plugin');
var combineLoaders = require('webpack-combine-loaders');

module.exports = {
  entry: [
    path.join(__dirname, 'src/main.js')
  ],
  output: {
    path: path.join(__dirname, '/dist/'),
    filename: '[name]-[hash].min.js',
    publicPath: ''
  },
  plugins: [
    new CopyWebpackPlugin([
        { from: 'locales/**', to: './' }
    ]),
    new webpack.optimize.OccurenceOrderPlugin(),
    new HtmlWebpackPlugin({
      template: 'src/index.tpl.html',
      inject: 'body',
      filename: 'index.html'
    }),
    new ExtractTextPlugin('[name]-[hash].min.css'),
    new webpack.optimize.UglifyJsPlugin({
      compressor: {
        warnings: false,
        screw_ie8: true
      }
    }),
    new StatsPlugin('webpack.stats.json', {
      source: false,
      modules: false
    }),
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
      'APISERVER': JSON.stringify(''),
      'SANKEYDOMAIN': JSON.stringify("sankey/")
    }),
    new ExtractTextPlugin('style.css', { allChunks: true }),
  ],
  module: {
    rules: [
      {
        test: /\.json$/,
        use: 'json-loader'
      }
    ],
    loaders: [{
      test: /\.jsx?$/,
      exclude: [
          /node_modules/,
          path.join(__dirname, 'src/js/components/Sankey/static')
      ],
      loader: 'babel',
      query: {
        cacheDirectory: true,
        plugins: ['transform-decorators-legacy' ],
        "presets": ["react", "es2015", "stage-0"]
      }
    }, {
      test: /\.json?$/,
      loader: 'json'
    },
    {
      test: /\.css$/,
      loader: 'css-loader',
      use: [ 'style-loader', 'css-loader' ]
    },
    {
      test: /\.sass$/,
      loader: ExtractTextPlugin.extract('css-loader?sourceMap!autoprefixer-loader?{browsers:["last 2 version"]}!sass-loader?indentedSyntax&sourceMap&sourceMapContents')
    },
    {
    test: /\.scss$/,
    loader: ExtractTextPlugin.extract(
      'style-loader',
      combineLoaders([{
        loader: 'css-loader',
        query: {
          modules: true,
          localIdentName: '[hash:base64:5]'
        }
      }, {
        loader: 'sass-loader'
        }, {
          loader: 'autoprefixer-loader',
          query: {
            browsers:'last 2 versions'
          }
        }])
      )
    },
    {
      test: /\.(jpe?g|ico|gif|png)[\?]?.*$/,
      loader: 'url-loader?limit=10000&name=assets/[name].[ext]'
    },
    {
      test: /\.(ttf|eot|svg|woff|woff2)[\?]?.*$/,
      loader: 'url-loader?limit=10000&name=assets/font/[name].[ext]'
    }
  ]},
  postcss: [
    require('autoprefixer')
  ]
};
