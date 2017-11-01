## CNDP - UI Project

### Stack
* React.js
* MobX
* For a complete list of dependencies visit package.json

## Installation
From root folder, perform the following steps.

Install dependencies:

```
$ npm install
```

Run locally:

```
$ npm start
```

Build to /dist:

```
$ npm run build
```

## Folders structure and naming conventions

## Basic architecture

### Per application:

* _Ui store -_ stores UI related information which is relevent for more than one container or component.
* _Data store -_ stores data from server converted by models.


###Per model:

* _Model -_ an exact representation of the data base model, decalring each field and foreign keys.
* _Handler -_ a middleware between component and data store.
* _Service -_ where fetch is being made.
* _Api -_ declaration of all REST API URLs related to that model.


## Adding a new component / container

* Create a new folder in /components or /container.
* Copy template-component.jsx from /templates to the new folder and rename it.
* Create scss file in the new folder.
* Export that file in /components/index.js or /containers/index.js

  * Component which depends on props should declare prop types, for example:

  ```
  static propTypes = {myProp: React.PropTypes.string.isRequired}
  ```

*Upon adding a new container, set related UI URL (hash) route in routes.js.*

## Adding a new model

*Same as in components and containers, only difference is the files copied.*
*Copy the following files from /templates and rename them accordingly:*

* tempate-service.js
* tempate-handler.js
* tempate-api.js
* tempate-model.js

*Follow naming convention in folder structure above.*

## Use a component / container / model

Examples:

```
import MyModel from 'path/to/models';
import MyContainer from 'path/to/containers';
import MyComponent from 'path/to/components';
```

## Styling

This project uses Css Modules.

To style a component:

*Import a stylesheet into the component (by convention it should be located at the same folder as the component).*
*Use class names from style sheet directly in component, for example:*

```
import styles from './styles-my-component.scss';
<div className={styles.Container}></div>
```
