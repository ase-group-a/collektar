import {GAME_SEARCH} from '../../constants'

describe('Game search works correctly', () => {
    beforeEach(() => {
        // Visit games page
        cy.visit('/media/games')
    })

    it('Sucessfully searches and displays game', () => {
        cy.get('main')
            .find('input[type="search"]')
            .should("exist")
            .should("have.attr", "placeholder", "Search")
            .type(GAME_SEARCH.searchTerm)
            .type('{enter}')
        
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src', GAME_SEARCH.imageSrc)
        
        cy.get('.card').first()
            .should("exist")
            .find("h2")
            .should("exist")
            .should("not.be.empty")
            .contains(GAME_SEARCH.gameTitle)
    });
})