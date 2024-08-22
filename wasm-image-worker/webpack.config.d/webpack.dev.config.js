config.resolve.conditionNames = ['require', 'import'];

config.module.rules.push({
    test: /imageWorker\.uninstantiated\.mjs$/,
    loader: 'string-replace-loader',
    options: {
        search: /isBrowser\s*=\s*.*?;/,
        replace: 'isBrowser=true;',
    }
});