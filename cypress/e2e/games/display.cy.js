describe('/media/games displays correctly', () => {
    beforeEach(() => {
        // Visit games
        cy.visit('/media/games')
    })
    
    it('Correctly navigates to games', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search games button
        cy.get('.navbar')
            .find('a[href="/media/games"]')
            .should('exist')
            .click()
    })

    it('At least one card exists and has correct image', () => {
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and("not.be.empty")
    })

    it('Game name is displaying', () => {
        cy.get('.card').first()
            .should('exist')
            .find('h2')
            .should('exist')
            .invoke('text')
            .should('not.be.empty')
    })
})