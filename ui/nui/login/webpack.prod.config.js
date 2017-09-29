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
      	'./app/index'
    ],

    output: {
        path: path.join(__dirname, 'public'),
        filename: 'main.js',
        publicPath: './'
    },

    plugins: [

        new webpack.HotModuleReplacementPlugin(),
        new webpack.NoErrorsPlugin(),
        new ExtractTextPlugin('./style.css', { allChunks: true }),
        new HtmlWebpackPlugin({
          title: 'Project Nazgul',
          template: './app/index.html'
        }),

		new webpack.DefinePlugin({
			APISERVER: JSON.stringify(""),
			PUBLICDOMAIN: JSON.stringify(""),
			SANKEYDOMAIN: JSON.stringify("sankey/")
		})
    ],

    module: {
        loaders: [
            {   test: /\.js?$/,
                loader: 'babel',
                exclude: [
                    /node_modules/,
                    path.join(__dirname, 'src/js/sankey'),
                    path.join(__dirname, 'src/js/data/world-country-names.tsv'),
                    path.join(__dirname, 'src/js/data/world-110m.json')
                ]
            },
            {
				test: /\.scss$/,
				loader: ExtractTextPlugin.extract('css!autoprefixer-loader!sass')
			},
			{
				test: /\.jpe?g$|\.gif$|\.png$|\.svg$|\.wav$|\.mp3$/,
				loader: 'file-loader?limit=10000&name=assets/[name].[ext]'
			},
            { test: /\.(ttf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
                loader: 'file?limit=10000&name=assets/[name].[ext]'}
        ]
    }
}
