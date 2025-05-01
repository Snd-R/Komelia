config.output.chunkFormat = false
config.output.asyncChunks = false
config.output.publicPath = ""
config.entry = {
    main: [
        require('path').resolve(__dirname, "kotlin/publicPath.js"),
        require('path').resolve(__dirname, "kotlin/background.mjs"),
    ]
};

Object.defineProperty(config, 'devtool', {
    get() {
        return false;
    },
    set() {
    },
});