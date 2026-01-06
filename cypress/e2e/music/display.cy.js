describe('/media/music displays correctly', () => {
    beforeEach(() => {
        // Visit music
        cy.visit('/media/music')
    })

    it('Correctly navigates to music', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search music button
        cy.get('.navbar')
            .find('a[href="/media/music"]')
            .should('exist')
            .click()
    })

    it('At least one song card exists and has correct image', () => {
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and("not.be.empty")
    })

    it('Song name is displaying', () => {
        cy.get('.card').first()
            .should('exist')
            .find('h2')
            .should('exist')
            .invoke('text')
            .should('not.be.empty')
    })
})