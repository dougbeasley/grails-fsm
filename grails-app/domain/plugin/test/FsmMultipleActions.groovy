package plugin.test

/**
 * Domain class to held some test of the Fsm plugin
 * This testcase adapted from Aleksandar Kochnev's propsed to cover a bug in
 * actions attached to transitions.
 */
class FsmMultipleActions {

    String status = 'loaded'
    Boolean hasErrors = false
    Boolean action1Called = false
    Boolean action2Called = false

    static fsm_def = [
	    status : [
	               loaded : { flow ->
	                   flow.on ('validate') {
	                       from('loaded').when({hasErrors}).to('in_error').act({
	                    	   delegate.setAction(1)
	                       })
	                       from('loaded').when({!hasErrors}).to('validated').act({
							   delegate.setAction(2)
	                       })
	                   }
	               }
	          ]
    ]

    def setAction(n) {
    	this."action${n}Called" = true
    }

}
