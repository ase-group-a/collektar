describe('GET /api/media/health', () => {
    beforeEach(() => {
        cy.fixture('general.json').as('general');
    });

    it('should return 200 and OK for /api/media/health', function () {
        cy.request({
            method: 'GET',
            url: `${this.general.mediaApi}/health`
        }).then((response) => {
            expect(response.status).to.equal(200);
            expect(response.body).to.equal('OK');
        });
    });

    it('should return 200 and OK for /api/auth/health', function () {
        cy.request({
            method: 'GET',
            url: `${this.general.authApi}/health`
        }).then((response) => {
            expect(response.status).to.equal(200);
            expect(response.body).to.equal('OK');
        });
    });

    it('should return 401 for /api/collections/health', function () {
        cy.request({
            method: 'GET',
            url: `${this.general.collectionApi}/health`
        }).then((response) => {
            expect(response.status).to.equal(401);
        });
    });
});