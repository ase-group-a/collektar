import {TITLE_BAR_SETTINGS} from '../../constants'

describe('Title bar displays correctly', () => {
    beforeEach(() => {
        // Visit homepage
        cy.visit('/')
    })

    it('Title bar is showing and contains elements', () => {
        cy.get('.navbar')
            .should("exist")
            .should("not.be.empty")

        cy.get('.navbar')
            .find("img")
            .should("exist")
            .should("have.attr", "src", TITLE_BAR_SETTINGS.logoSrc)

        cy.get('.navbar')
            .find(`a[href="${TITLE_BAR_SETTINGS.games.href}"]`)
            .should("exist")
            .should("not.be.empty")
            .contains(TITLE_BAR_SETTINGS.games.name)

        cy.get('.navbar')
            .find(`a[href="${TITLE_BAR_SETTINGS.movies.href}"]`)
            .should("exist")
            .should("not.be.empty")
            .contains(TITLE_BAR_SETTINGS.movies.name)

        cy.get('.navbar')
            .find(`a[href="${TITLE_BAR_SETTINGS.shows.href}"]`)
            .should("exist")
            .should("not.be.empty")
            .contains(TITLE_BAR_SETTINGS.shows.name)

        cy.get('.navbar')
            .find(`a[href="${TITLE_BAR_SETTINGS.music.href}"]`)
            .should("exist")
            .should("not.be.empty")
            .contains(TITLE_BAR_SETTINGS.music.name)

        cy.get('.navbar')
            .find(`a[href="${TITLE_BAR_SETTINGS.books.href}"]`)
            .should("exist")
            .should("not.be.empty")
            .contains(TITLE_BAR_SETTINGS.books.name)

        cy.get('.navbar')
            .find(`.btn`)
            .should("exist")
            .should("not.be.empty")
            .contains(TITLE_BAR_SETTINGS.login.name)
    });
})