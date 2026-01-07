const {defineConfig} = require('cypress');

module.exports = defineConfig({
    projectId: '6gffrg',
    e2e: {
        baseUrl: 'http://localhost',
        retries: {
            runMode: 3,
        },
        viewportHeight: 1080,
        viewportWidth: 1920,
        setupNodeEvents(on, config) {
            // implement node event listeners here
        },
    },
});
