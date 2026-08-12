config.output.chunkFormat = false
config.output.asyncChunks = false
config.output.publicPath = ""
config.entry = {
    main: [
        require('path').resolve(__dirname, "kotlin/publicPath.js"),
        require('path').resolve(__dirname, "kotlin/popup.mjs"),
    ]
};
