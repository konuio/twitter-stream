import 'babel-polyfill';
import './reset.scss';
import React from 'react';
import ReactDOM from 'react-dom';
import WordCloud from './containers/WordCloud';

ReactDOM.render(<WordCloud />, document.getElementById('content'));
