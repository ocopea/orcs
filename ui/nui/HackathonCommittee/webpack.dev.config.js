// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var path              = require('path');
var webpack           = require('webpack');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var HtmlWebpackPlugin = require('html-webpack-plugin');

console.log("developement")

module.exports = {

    entry: [
        'webpack/hot/dev-server',
        'webpack-hot-middleware/client',
        './app/index'
    ],

	output: {
		path: path.resolve('build'),
		publicPath: '/',
		filename: "main.js"
	},

	plugins: [
		new webpack.HotModuleReplacementPlugin(),
		new webpack.NoErrorsPlugin(),
		new ExtractTextPlugin('./css/style.css', { allChunks: true }),

		new webpack.DefinePlugin({
			APISERVER: JSON.stringify("http://localhost:8083/dd/committee-api"),
			PUBLICDOMAIN: JSON.stringify("public/"),
			SANKEYDOMAIN: JSON.stringify("public/sankey/")
		})

	],

	module: {
		preLoaders: [
			//{ test: /\.js$/, exclude: /node_modules/, loader: 'jshint-loader' }
		],
		loaders: [
			{
				test: /\.js?$/,
				loaders: ['react-hot', 'babel'],
				exclude: /node_modules/
			},
			{
				test: /\.js$/,
				exclude: /node_modules/,
				loader: 'babel-loader'
			},
			{
				test: /\.scss$/,
				loader: ExtractTextPlugin.extract('css!autoprefixer-loader!sass')
			},
			{
				test: /\.(jpe?g|gif|png|eot|svg|woff|woff2)[\?]?.*$/,
				loader: 'url-loader?limit=10000&name=assets/[name].[ext]'
			},
			{
				test: /\.(ttf)[\?]?.*$/,
				loader: 'url-loader?limit=10000&name=font/[name].[ext]'
			}
		]
	}

};
