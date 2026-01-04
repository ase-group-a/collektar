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
});