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
        .then((card) => {
            const itemImgSrc = card.find('img').attr('src')
            const itemTitle = card.find('h2').text()

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

Cypress.Commands.add('collectionItemTest', (mediaType) => {
    cy.wait(500)

    cy.visit(`/media/${mediaType}`)

    cy.get('.card').first()
        .then((card) => {

            // Save image src and title of media item card 1 for comparison
            const item1ImgSrc = card.find('img').attr('src')
            const item1Title = card.find('h2').text()
            expect(item1ImgSrc).to.not.be.empty
            expect(item1Title).to.not.be.empty

            // Add item 1 to collection
            cy.get('.card')
                .first()
                .find('button')
                .should("exist")
                .click()

            cy.get('.card').eq(1) // Get second card
                .then((card2) => {
                    // Save image src and title for second media card
                    const item2ImgSrc = card2.find('img').attr('src')
                    const item2Title = card2.find('h2').text()
                    expect(item2ImgSrc).to.not.be.empty
                    expect(item2Title).to.not.be.empty

                    // Add item 2 to collection
                    cy.get('.card')
                        .eq(1)
                        .find('button')
                        .should("exist")
                        .click()

                    // Navigate to collections
                    cy.get('.navbar')
                        .get(`a`)
                        .contains("My Collections")
                        .should("exist")
                        .click()

                    // Select collection and open it
                    cy.get('main')
                        .find("h3")
                        .contains(new RegExp(`^${mediaType}$`))
                        .should('exist')
                        .click()

                    // Wait for loading to finish
                    cy.wait(500)
                    
                    // Check card 1
                    cy.get('.card')
                        .find(`img[src="${item1ImgSrc}"]`)
                        .should('exist')
                        
                    cy.get('.card')
                        .find("h2")
                        .contains(item1Title)
                        .should("exist")

                    // Check card 2
                    cy.get('.card')
                        .find(`img[src="${item2ImgSrc}"]`)
                        .should('exist')

                    cy.get('.card')
                        .find("h2")
                        .contains(item2Title)
                        .should("exist")
                    
                    // Remove media item 1
                    cy.get('.card').first()
                        .then((card) => {

                            // Save image src and title of media item card 1 for comparison
                            const itemImgSrc = card.find('img').attr('src')
                            const itemTitle = card.find('h2').text()
                            expect(itemImgSrc).to.not.be.empty
                            expect(itemTitle).to.not.be.empty

                            cy.get('.card')
                                .first()
                                .find('button')
                                .should("exist")
                                .click()

                            cy.reload()

                            // Check that the remaining card is not the deleted media item
                            cy.get('.card')
                                .first()
                                .find(`img[src="${itemImgSrc}"]`)
                                .should('not.exist')

                            cy.get('.card')
                                .first()
                                .should("exist")
                                .find("h2")
                                .contains(itemTitle)
                                .should("not.exist")
                            
                            // Check that no other card exists
                            cy.get('.card')
                                .eq(2)
                                .should("not.exist")
                        })
                })
        })
})

Cypress.Commands.add('registerUser', (user, navigateBack = true, logoutAfterRegister = true) => {
    // Save current url
    let url
    cy.url().then(u => {
        url = u
    })
    
    // Open register page
    cy.visit('/register')

    cy.get('main')
        .find('input[type="email"]')
        .should("exist")
        .type(user.email)

    cy.get('main')
        .find('input[placeholder="Your unique username"]')
        .should("exist")
        .type(user.username)

    cy.get('main')
        .find('input[placeholder="Your display name"]')
        .should("exist")
        .type(user.displayName)

    cy.get('main')
        .find('input[placeholder="Create a password"]')
        .should("exist")
        .type(user.password)

    cy.get('main')
        .find('input[placeholder="Repeat your password"]')
        .should("exist")
        .type(user.password)
    
    cy.get('main')
        .find('button[type="submit"]')
        .should("exist")
        .click()
    
    if (logoutAfterRegister) {
        cy.get('.navbar')
            .get('.dropdown')
            .should("exist")
            .should("not.be.empty")
            .click()

        cy.get('.navbar')
            .get('.dropdown')
            .find('button')
            .contains('Logout')
            .click()
    }
    
    // Navigate back to origin page
    if (navigateBack) {
        cy.then(() => {
            cy.visit(url)
        })
    }
})

const usedNumbers = new Set()
Cypress.Commands.add('getRandomUser', (user) => {
    let random
    
    // Make sure random numbers do not repeat
    do {
        random = Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);
    } while (usedNumbers.has(random))
    usedNumbers.add(random)

    return {
        email: `${random}${user.email}`,
        username: `${user.username}${random}`,
        displayName: `${user.displayName}${random}`,
        password: `${user.password}${random}`
    }
})