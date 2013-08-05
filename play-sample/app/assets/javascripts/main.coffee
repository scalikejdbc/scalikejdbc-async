#
# Backbone main.js
#

# --- Router ---

AppRouter = Backbone.Router.extend
  routes:
    ""                       : "showProgrammers"
    "programmers"            : "showProgrammers"
    "companies"              : "showCompanies"
    "skills"                 : "showSkills"

  showProgrammers: () -> new ProgrammersView().renew()
  showCompanies:   () -> new CompaniesView().renew()
  showSkills:      () -> new SkillsView().renew()

# --- Programmers ---

Programmer = Backbone.Model.extend
  urlRoot: '/programmers'

  validate: (attrs, options) ->
    'name is required' unless attrs.name

  create: () ->
    route = jsRoutes.controllers.Programmers.create()
    $.ajax
      url: route.url, type: route.method, data: @attributes,
      success: (response) -> new ProgrammersView().renew()
      error:   (response) -> window.alert("Failed to add a programmer because of #{response.responseText}!")

  addSkill: (skillId) ->
    if @id
      id = @id
      route = jsRoutes.controllers.Programmers.addSkill(id, skillId)
      $.ajax
        url: route.url, type: route.method,
        success: (response) -> new ProgrammersView().renew()
        error:   (response) -> console.log("POST /programmers/#{id}/skills/#{skillId} failure (status: #{response.statusText})")
    else window.alert('id should be specified!')

  deleteSkill: (skillId) ->
    if @id
      id = @id
      route = jsRoutes.controllers.Programmers.deleteSkill(id, skillId)
      $.ajax
        url: route.url, type: route.method,
        success: (response) -> new ProgrammersView().renew()
        error:   (response) -> console.log("DELETE /programmers/#{id}/skills/#{skillId} failure (status: #{response.statusText})")
    else window.alert('id should be specified!')

  changeCompany: (companyId) ->
    if @id
      id = @id
      if companyId then route = jsRoutes.controllers.Programmers.joinCompany(id, companyId)
      else              route = jsRoutes.controllers.Programmers.leaveCompany(id)
      $.ajax
        url: route.url, type: route.method,
        success: (response) -> $("#changeCompanyHolder#{id}").append($('<i class="icon-ok"></i>'))
        error:   (response) -> console.log("/programmers/#{id}/company failure (status: #{response.statusText})")
    else window.alert('id should be specified!')

  delete: () ->
    if @id
      id = @id
      route = jsRoutes.controllers.Programmers.delete(id)
      $.ajax
        url: route.url, type: route.method,
        success: (response) -> new ProgrammersView().renew()
        error:   (response) -> console.log("DELETE /programmers/#{id} failure (status: #{response.statusText})")
    else window.alert('id should be specified!')

Programmers = Backbone.Collection.extend
  model: Programmer
  url: '/programmers'

ProgrammerView = Backbone.View.extend
  render: (param) -> $('#main').html(@$el.html(_.template($('#main_programmer').html(), param)))

ProgrammersView = Backbone.View.extend
  events:
    "submit  .addProgrammer"    : "addProgrammer"
    "blur    .addSkill"         : "addSkill"
    "click   .deleteSkill"      : "deleteSkill"
    "blur    .changeCompany"    : "changeCompany"
    "click   .deleteProgrammer" : "deleteProgrammer"

  addProgrammer: (event) ->
    event.preventDefault()
    data = { name: $('#newName').val(), companyId: $('#newCompanyId').val() }
    model = new Programmer(data)
    if model.isValid()
      model.create()
    else 
      window.alert(model.validationError)

  addSkill: (event) ->
    event.preventDefault()
    id = $(event.currentTarget).attr('data-id')
    skillId = $(event.currentTarget).val()
    new Programmer(id: id).addSkill(skillId)

  deleteSkill: (event) ->
    event.preventDefault()
    id = $(event.currentTarget).attr('data-programmer-id')
    skillId = $(event.currentTarget).attr('data-skill-id')
    new Programmer(id: id).deleteSkill(skillId)

  changeCompany: (event) ->
    event.preventDefault()
    id = $(event.currentTarget).attr('data-id')
    companyId = $(event.currentTarget).val()
    new Programmer(id: id).changeCompany(companyId)

  deleteProgrammer: (event) ->
    if window.confirm("Are you sure?") 
      event.preventDefault()
      id = $(event.currentTarget).attr('data-id')
      new Programmer(id: id).delete()

  render: (param) -> 
    @$el.html(_.template($('#main_programmers').html(), param))

  renew: () ->
    $.when(
      new Programmers().fetch(), new Companies().fetch(), new Skills().fetch()
    ).done((ps, cs, ss) ->
      $('#main').html(new ProgrammersView().render(
        programmers: new Programmers(ps[0])
        companies:   new Companies(cs[0])
        skills:      new Skills(ss[0])
      ))
    )

# --- Companies ---

Company = Backbone.Model.extend
  urlRoot: '/companies'

  validate: (attrs, options) ->
    'name is required' unless attrs.name

  create: () ->
    route = jsRoutes.controllers.Companies.create()
    $.ajax
      url: route.url, type: route.method, data: @attributes,
      success: (response) -> new CompaniesView().renew()
      error: (response)   -> window.alert("Failed to add a company because of #{response.responseText}!")

  delete: () ->
    if @id
      id = @id
      route = jsRoutes.controllers.Companies.delete(id)
      $.ajax
        url: route.url, type: route.method,
        success: (response) -> new CompaniesView().renew()
        error: (response) -> console.log("DELETE /companies/#{id} failure (status: #{response.statusText})")
    else window.alert('id should be specified!')

Companies = Backbone.Collection.extend
  model: Company
  url: '/companies'

CompaniesView = Backbone.View.extend
  events:
    "submit  .addCompany"    : "addCompany"
    "click   .deleteCompany" : "deleteCompany"

  addCompany: (event) ->
    event.preventDefault()
    data = {name: $('#newName').val()}
    model = new Company(data)
    if model.isValid() 
      model.create()
    else 
      window.alert(model.validationError)

  deleteCompany: (event) ->
    if window.confirm("Are you sure?")
      event.preventDefault()
      id = $(event.currentTarget).attr('data-id')
      new Company(id: id).delete()

  render: (param) -> 
    $('#main').html(@$el.html(_.template($('#main_companies').html(), param)))

  renew: () ->
    new Companies().fetch
      success: (companies) -> new CompaniesView().render({companies: companies})
      error:   (response) -> console.log("GET /companies failure (status: #{response.statusText})")

# --- Skills ---

Skill = Backbone.Model.extend
  urlRoot: '/skills'

  validate: (attrs, options) ->
    'name is required' unless attrs.name

  create: () ->
    route = jsRoutes.controllers.Skills.create()
    $.ajax
      url: route.url, type: route.method, data: @attributes,
      success: (response) -> new SkillsView().renew()
      error: (response) -> window.alert("Failed to add a skill because of #{response.responseText}!")

  delete: () ->
    if @id
      id = @id
      route = jsRoutes.controllers.Skills.delete(id)
      $.ajax
        url: route.url, type: route.method,
        success: (response) -> new SkillsView().renew()
        error: (response) -> console.log("DELETE /skills/#{id} failure (status: #{response.statusText})")
    else window.alert('id should be specified!')

Skills = Backbone.Collection.extend
  model: Skill
  url: '/skills'

SkillsView = Backbone.View.extend
  events:
    "submit  .addSkill"    : "addSkill"
    "click   .deleteSkill" : "deleteSkill"

  addSkill: (event) ->
    event.preventDefault()
    data = {name: $('#newName').val()}
    model = new Skill(data)
    if model.isValid()
      model.create()
    else
      window.alert(model.validationError)

  deleteSkill: (event) ->
    if window.confirm("Are you sure?")
      event.preventDefault()
      id = $(event.currentTarget).attr('data-id')
      new Skill(id: id).delete()

  render: (param) -> 
    $('#main').html(@$el.html(_.template($('#main_skills').html(), param)))

  renew: () ->
    new Skills().fetch
      success: (skills) -> new SkillsView().render({skills: skills})
      error: (response) -> console.log("/skills failure (status: #{response.statusText})")

# --- Initialize ---

$ () ->
  appRouter = new AppRouter()
  Backbone.history.start
    pushHistory: true

