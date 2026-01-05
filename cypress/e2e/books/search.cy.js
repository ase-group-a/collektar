import {BOOK_SEARCH} from '../../constants'

describe('Book search works correctly', () => {
    beforeEach(() => {
        // Visit books page
        cy.visit('/media/books')
    })

    it('Sucessfully searches and displays books', () => {
        cy.get('main')
            .find('input[type="search"]')
            .should("exist")
            .should("have.attr", "placeholder", "Search")
            .type(BOOK_SEARCH.searchTerm)
            .type('{enter}')
        
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and('match', BOOK_SEARCH.imageSrc)
        
        cy.get('.card').first()
            .should("exist")
            .find("h2")
            .should("exist")
            .should("not.be.empty")
            .contains(BOOK_SEARCH.bookTitle)
    });
})