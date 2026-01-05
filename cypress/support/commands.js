// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

Cypress.Commands.add('paginationTest', () => {

    // Find and store element values
    cy.get('.card').first()
        .then(($card) => {
            const itemImgSrc = $card.find('img').attr('src')
            const itemTitle = $card.find('h2').text()

            expect(itemImgSrc).to.not.be.empty
            expect(itemTitle).to.not.be.empty

            // Find page buttons and click second page
            cy.get('.join')
                .should("exist")
                .should("not.be.empty")
                .find('button')
                .contains('2')
                .click()

            // Find and compare element values of page 2 (should not be the same)
            cy.get('.card').first()
                .should('exist')
                .find('img')
                .should("not.have.attr", "src", itemImgSrc)

            cy.get('.card').first()
                .should('exist')
                .find('h2')
                .should("not.contain.text", itemTitle)
        })
})