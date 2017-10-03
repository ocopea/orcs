// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Quota from './quota.comp.js';
import Actions from '../../actions/dev-actions.js';


var Quotas = React.createClass({

    getInfraQuotas: function(){

		const infraQuotas = this.getTopThreeQuotas(this.props.quotas.infrastructureQuotas);

    var quotas = infraQuotas.map((quota, i)=>{
        return <div className="quota" key={i}>
                    <Quota
                        key={i}
                        name={quota.name}
                        strokeColor="#7ed396"
                        precent={quota.precent}/>
               </div>
    });
    return quotas;
  },

	getTopThreeQuotas: function(infraQuotas){

		var sortedQuotas = _.sortBy(infraQuotas, "precent");
		sortedQuotas = _.reverse(sortedQuotas);
		sortedQuotas = _.take(sortedQuotas, 3);
		return sortedQuotas;

	},

    render: function(){

        return(

        	<div>
				<div className="quotas">
					<div className="title">
						<div>org</div>
						<div>OCOPEA</div>
					</div>

					<div className="quota">
						<Quota
							strokeColor="#8cc9ea"
							name="app quota"
							precent={this.props.quotas.appQuota}/>
					</div>

					{this.getInfraQuotas()}

				</div>

				{
					this.props.quotas.infrastructureQuotas.length > 3 ?
						<div className="btn-getAllQuoats link" onClick={Actions.showQuotasList}>
							<span></span>
							<span></span>
						</div>
				    :
					null
				}

			</div>
        )

    },

});

export default Quotas;
