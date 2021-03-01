/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

const parser = new DOMParser();

function writeValue(value, name) {
  if (name.endsWith("Date")) {
    let parts = value.split('-');
    if (parts.length === 3) {
      value = new Date(parts[2], parts[1] - 1, parts[0] - 1);
    }
  }
  return value;
}

function readValue(value, name) {
  if (name.endsWith("Date")) {
    const date = new Date(Date.parse(value))
    if (date.getFullYear() > 0) {
      value = (date.getDate() + 1) + '-' + (date.getMonth() + 1) + '-' + date.getFullYear();
    }
  }
  return value
}

const formToJson = (form) => {
  return Array.from(form.querySelectorAll('input, select, textarea'))
    .filter(element => element.name)
    .reduce((json, element) => {
      let value = element.type === 'checkbox' ? element.checked : element.value;
      json[element.name] = writeValue(value, element.name);
      return json;
    }, {});
}

let parseHtml = function (html) {
  let document1 = parser.parseFromString(html, 'text/html');
  let element = document1.body.firstElementChild;
  return element;
};

function show(selector) {
  document.querySelectorAll(selector).forEach((el) => {
    el.style['display'] = ''
  })
}

function hide(selector) {
  document.querySelectorAll(selector).forEach((el) => {
    el.style['display'] = 'none'
  })
}

function enableMaterializeElements() {
  M.FormSelect.init(document.querySelectorAll('select'));
  M.Datepicker.init(document.querySelectorAll('.datepicker'), {
    defaultDate: new Date(1975, 6, 26),
    maxDate: new Date(),
    autoClose: true,
    format: 'd-m-yyyy'
  });
}

function updateForm() {
  M.updateTextFields();
  let textAreas = document.querySelectorAll('textarea');
  textAreas.forEach((textarea) => {
    M.textareaAutoResize(textarea);
  })
  M.FormSelect.init(document.querySelectorAll('select'));
}

const registerFormSubmitListeners = (callback) => {
  const formElements = document.getElementsByTagName('form');
  for (const i in formElements) {
    const form = formElements[i];
    form.onsubmit = (e) => {
      const form = e.target;
      fetch(form.getAttribute('action'), {
        method: 'PUT',
        body: JSON.stringify(formToJson(form)),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(callback);
      return false;
    }
  }
};

const jsonResponseHandler = (response) => {
  if (!response.ok) {
    if (response.status === 403) {
      let res = window.confirm("It seems you are logged out, go to the login page?")
      if (res) {
        window.location = '/login';
      }
    }
    throw Error(response.statusText);
  }
  return response.json();
}

const jsonPublicResponseHandler = (response) => {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response.json();
}

