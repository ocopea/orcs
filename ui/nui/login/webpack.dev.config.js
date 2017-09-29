// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin');

console.log("develpement")


module.exports = {
    devtool: 'source-map',

    entry: [
        'webpack/hot/dev-server',
        'webpack-hot-middleware/client',
        './app/index'
    ],

    output: {
        path: path.resolve('public'),
        filename: 'main.js',
        publicPath: '/'
    },

    plugins: [
        new webpack.HotModuleReplacementPlugin(),
        new webpack.NoErrorsPlugin(),
        new ExtractTextPlugin('./style.css', { allChunks: true }),

    		new webpack.DefinePlugin({
    			APISERVER: JSON.stringify("http://localhost:8083"),
    		})
    ],

    module: {
        loaders: [
            {   test: /\.js?$/,
                loader: 'babel',
                exclude: [
                    /node_modules/
                ]
            },
            {
				test: /\.scss$/,
				loader: ExtractTextPlugin.extract('css!autoprefixer-loader!sass')
			},
			{
				test: /\.(jpe?g|gif|png|ttf|eot|woff|woff2)[\?]?.*$/,
				loader: 'url-loader?limit=10000&name=assets/[name].[ext]'
			}
        ]
    }
}
