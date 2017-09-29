// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';
const shakeLeft = ' Topology__element--shake-left';
const shakeRight = ' Topology__element--shake-right';
const alertContainer = '.services-no-deps';
const message = 'no data services'


export default function shakeElement(domElement){

  let toggle = false;
  let count = 0;
  let timer;

  (function shake(){
    timer = setTimeout(function(){
      if(count < 3){
        shakeIt(count);
        shake();
        count++;
      }else{
        clearClassNames();

        // remove alert
        setTimeout(()=>{ $(alertContainer).remove() }, 4000);
        clearInterval(timer);
      }
    }, 200);

  })();

  // add classes to initiate animation
  function shakeIt(count){
    toggle = !toggle;
    if(toggle){
      $(domElement).removeClass(shakeRight)
      domElement.className = domElement.className + shakeLeft;
    }else{
      $(domElement).removeClass(shakeLeft)
      domElement.className = domElement.className + shakeRight;
    }
  }

  shakeIt();

  function clearClassNames(){
    count = 0;
    $(domElement).removeClass(shakeRight);
    $(domElement).removeClass(shakeLeft);
  }
}
