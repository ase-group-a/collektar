describe('/media/shows displays correctly', () => {
    beforeEach(() => {
        // Visit shows
        cy.visit('/media/shows')
    })

    it('Correctly navigates to shows', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search shows button
        cy.get('.navbar')
            .find('a[href="/media/shows"]')
            .should('exist')
            .click()
    })

    it('At least one show card exists and has correct image', () => {
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and("not.be.empty")
    })

    it('Show name is displaying', () => {
        cy.get('.card').first()
            .should('exist')
            .find('h2')
            .should('exist')
            .invoke('text')
            .should('not.be.empty')
    })
})