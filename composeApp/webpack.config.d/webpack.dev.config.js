config.devServer = Object.assign(
    {},
    config.devServer || {},
    {
        headers: {
            'Cross-Origin-Embedder-Policy': 'require-corp',
            'Cross-Origin-Opener-Policy': 'same-origin',
        },
        proxy: [
            {
                context: ['/api'],
                target: 'http://localhost:25600',
            },
        ],

    }
)
config.resolve.conditionNames = ['require', 'import'];