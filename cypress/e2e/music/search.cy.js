import {MUSIC_SEARCH} from '../../constants'

describe('Music search works correctly', () => {
    beforeEach(() => {
        // Visit music page
        cy.visit('/media/music')
    })

    it('Sucessfully searches and displays songs', () => {
        cy.get('main')
            .find('input[type="search"]')
            .should("exist")
            .should("have.attr", "placeholder", "Search")
            .type(MUSIC_SEARCH.searchTerm)
            .type('{enter}')
        
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src', MUSIC_SEARCH.imageSrc)
        
        cy.get('.card').first()
            .should("exist")
            .find("h2")
            .should("exist")
            .should("not.be.empty")
            .contains(MUSIC_SEARCH.songTitle)
    });
})